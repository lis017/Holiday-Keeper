package com.planitsquare.holidaykeeper.scheduler;

import com.planitsquare.holidaykeeper.api.NagerApiClient;
import com.planitsquare.holidaykeeper.api.dto.AvailableCountryDto;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.country.Country;
import com.planitsquare.holidaykeeper.country.CountryService;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepository;
import com.planitsquare.holidaykeeper.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 외부 API 기반 앱 시작시 데이터 적재
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final NagerApiClient nagerApiClient;
    private final CountryService countryService;
    private final HolidayService holidayService;
    private final HolidayRepository holidayRepository;

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Override
    public void run(String... args) throws Exception {
        // 최초 1회만 전체 적재를 수행하기 위해 기존 Holiday 데이터 존재 여부 확인
        long existingHolidayCount = holidayRepository.count();

        if (existingHolidayCount > 0) {
            log.info("기존 Holiday 데이터가 {}건 존재하므로 DataLoader 초기 적재를 스킵합니다.", existingHolidayCount);
            return;
        }

        log.info("기존 Holiday 데이터가 없어 DataLoader 초기 적재를 시작합니다.");
        loadCountriesFromApi();
        log.info("Country 적재 로직 완료 후, Holiday 적재 시작");
        loadHolidaysFromApi();
        log.info("DataLoader 종료");
    }

    /** 외부 API 기반 국가 데이터 적재 */
    private void loadCountriesFromApi() {
        log.info("외부 API 기반 Country 데이터 적재 시작");
        List<AvailableCountryDto> countryDtos = nagerApiClient.getAvailableCountries();
        List<Country> countries = new ArrayList<>();
        for (AvailableCountryDto dto : countryDtos) {
            countries.add(Country.builder()
                    .countryCode(dto.countryCode())
                    .name(dto.name())
                    .build());
        }
        countryService.saveAll(countries);
        log.info("외부 API 기반 Country 데이터 적재 완료: {}개", countries.size());
    }

    /** 2020~2025 공휴일 데이터 외부 API에서 적재 */
    private void loadHolidaysFromApi() {
        // 전체 작업 시작 로그
        log.info("외부 API 기반 Holiday 데이터 적재 시작");

        List<Country> countries = countryService.findAll();
        log.info("Holiday 적재 대상 Country 개수: {}", countries.size());

        List<Holiday> holidays = new ArrayList<>();
        int startYear = 2020;
        int endYear = 2025;

        for (Country country : countries) {
            log.info("Country 처리 시작: code={}, name={}", country.getCountryCode(), country.getName());

            for (int year = startYear; year <= endYear; year++) {
                log.info("공휴일 조회 시작: country={}, year={}", country.getCountryCode(), year);

                List<PublicHolidayDto> holidayDtos = nagerApiClient.getPublicHolidays(year, country.getCountryCode());

                // Nager API 결과 로그 (null 대비 방어 코드)
                if (holidayDtos == null) {
                    log.warn("공휴일 조회 결과가 null입니다. country={}, year={}", country.getCountryCode(), year);
                    continue;
                }
                log.info("공휴일 조회 완료: country={}, year={}, 개수={}", country.getCountryCode(), year, holidayDtos.size());

                for (PublicHolidayDto dto : holidayDtos) {
                    holidays.add(Holiday.builder()
                            .countryCode(country)                        // Country 엔티티 참조
                            .date(LocalDate.parse(dto.date()))           // String → LocalDate
                            .localName(dto.localName())
                            .name(dto.name())
                            .global(dto.global())
                            .fixed(dto.fixed())
                            .counties(dto.counties())                    // List<String> or null 그대로 매핑
                            .launchYear(dto.launchYear())                // null 가능
                            .types(dto.types())                          // List<String>
                            .build());
                }
            }

            log.info("Country 처리 완료: code={}", country.getCountryCode());
        }

        log.info("Holiday 엔티티 saveAll 시작, 총 개수={}", holidays.size());
        holidayService.saveAll(holidays);
        log.info("외부 API 기반 Holiday 데이터 적재 완료: {}개", holidays.size());
    }
}