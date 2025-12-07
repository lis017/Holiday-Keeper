package com.planitsquare.holidaykeeper.scheduler;

import com.planitsquare.holidaykeeper.api.NagerApiClient;
import com.planitsquare.holidaykeeper.api.dto.AvailableCountryDto;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.country.Country;
import com.planitsquare.holidaykeeper.country.CountryService;
import com.planitsquare.holidaykeeper.holiday.Holiday;
import com.planitsquare.holidaykeeper.holiday.HolidayService;
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

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Override
    public void run(String... args) throws Exception {
        loadCountriesFromApi();
        loadHolidaysFromApi();
    }

    /** 외부 API 기반 국가 데이터 적재 */
    private void loadCountriesFromApi() {
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
        List<Country> countries = countryService.findAll();
        List<Holiday> holidays = new ArrayList<>();
        int startYear = 2020;
        int endYear = 2025;

        for (Country country : countries) {
            for (int year = startYear; year <= endYear; year++) {
                List<PublicHolidayDto> holidayDtos = nagerApiClient.getPublicHolidays(year, country.getCountryCode());
                for (PublicHolidayDto dto : holidayDtos) {
                    holidays.add(Holiday.builder()
                            .countryCode(country)                        // Country 엔티티 참조
                            .date(LocalDate.parse(dto.date()))           // String → LocalDate
                            .localName(dto.localName())
                            .name(dto.name())
                            .global(dto.global())
                            .fixed(dto.fixed())
                            .counties(dto.counties())                    // List<String> or null 그대로 매핑
                            .launchYear(dto.launchYear())                // null 가능 → OK
                            .types(dto.types())                          // List<String>
                            .build());
                }
            }
        }

        holidayService.saveAll(holidays);
        log.info("외부 API 기반 Holiday 데이터 적재 완료: {}개", holidays.size());
    }
}