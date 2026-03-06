package com.planitsquare.holidaykeeper.scheduler;

import com.planitsquare.holidaykeeper.api.NagerApiClient;
import com.planitsquare.holidaykeeper.api.dto.AvailableCountryDto;
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
import reactor.core.publisher.Flux;

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

    // 동시에 호출할 최대 API 요청 수 (병렬도 조절 포인트)
    private static final int PARALLEL_CONCURRENCY = 1;

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

        // 단일 호출이므로 block() 허용
        List<AvailableCountryDto> countryDtos = nagerApiClient.getAvailableCountries()
                .collectList()
                .block();

        List<Country> countries = new ArrayList<>();
        if (countryDtos != null) {
            for (AvailableCountryDto dto : countryDtos) {
                countries.add(Country.builder()
                        .countryCode(dto.countryCode())
                        .name(dto.name())
                        .build());
            }
        }
        countryService.saveAll(countries);
        log.info("외부 API 기반 Country 데이터 적재 완료: {}개", countries.size());
    }

    /** 2020~2025 공휴일 데이터 외부 API에서 병렬 적재 */
    private void loadHolidaysFromApi() {
        log.info("외부 API 기반 Holiday 데이터 적재 시작 (병렬도={})", PARALLEL_CONCURRENCY);

        List<Country> countries = countryService.findAll();
        log.info("Holiday 적재 대상 Country 개수: {}", countries.size());

        int startYear = 2020;
        int endYear = 2025;

        // ── 외부 API 호출 시작 시각 기록 ──────────────────────────────
        long apiStartTime = System.currentTimeMillis();
        log.info("[시간측정] 외부 API 호출 시작");

        // country × year 조합을 PARALLEL_CONCURRENCY 개씩 동시에 API 호출
        List<Holiday> holidays = Flux.fromIterable(countries)
                .flatMap(country ->
                        Flux.range(startYear, endYear - startYear + 1)
                                .flatMap(year ->
                                        nagerApiClient.getPublicHolidays(year, country.getCountryCode())
                                                .map(dto -> Holiday.builder()
                                                        .countryCode(country)
                                                        .date(LocalDate.parse(dto.date()))
                                                        .localName(dto.localName())
                                                        .name(dto.name())
                                                        .global(dto.global())
                                                        .fixed(dto.fixed())
                                                        .counties(dto.counties())
                                                        .launchYear(dto.launchYear())
                                                        .types(dto.types())
                                                        .build())
                                                .doOnError(e -> log.warn("공휴일 조회 실패 (스킵): country={}, year={}", country.getCountryCode(), year, e))
                                                .onErrorComplete() // 개별 실패 시 전체 중단 대신 해당 건만 스킵
                                )
                , PARALLEL_CONCURRENCY)
                .collectList()
                .block();

        // ── 외부 API 호출 완료 시각 기록 ──────────────────────────────
        long apiElapsedMs = System.currentTimeMillis() - apiStartTime;
        log.info("[시간측정] 외부 API 호출 완료 - 소요시간: {}ms ({}초)", apiElapsedMs, apiElapsedMs / 1000);

        if (holidays == null) {
            log.warn("Holiday API 적재 결과가 null입니다. 저장을 스킵합니다.");
            return;
        }

        // ── DB saveAll 시작 시각 기록 ──────────────────────────────────
        long saveStartTime = System.currentTimeMillis();
        log.info("[시간측정] DB saveAll 시작, 총 개수={}", holidays.size());

        holidayService.saveAll(holidays);

        // ── DB saveAll 완료 시각 기록 ──────────────────────────────────
        long saveElapsedMs = System.currentTimeMillis() - saveStartTime;
        log.info("[시간측정] DB saveAll 완료 - 소요시간: {}ms ({}초)", saveElapsedMs, saveElapsedMs / 1000);

        long totalElapsedMs = apiElapsedMs + saveElapsedMs;
        log.info("[시간측정] 전체 Holiday 적재 완료 - API호출: {}ms / DB저장: {}ms / 합계: {}ms ({}초)",
                apiElapsedMs, saveElapsedMs, totalElapsedMs, totalElapsedMs / 1000);
    }
}
