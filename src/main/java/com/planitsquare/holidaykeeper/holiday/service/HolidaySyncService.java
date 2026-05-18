package com.planitsquare.holidaykeeper.holiday.service;

import com.planitsquare.holidaykeeper.api.NagerApiClient;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.country.Country;
import com.planitsquare.holidaykeeper.country.CountryRepository;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

/*
    필터검색시 백그라운드로 외부 API 재조회후 Upsert
 */
@Service
@RequiredArgsConstructor
public class HolidaySyncService {

    // Holiday 적재 관련 로그는 DEBUG 레벨로만 남겨서 기본 INFO 로그를 지저분하게 만들지 않음
    private static final Logger log = LoggerFactory.getLogger(HolidaySyncService.class);

    private final NagerApiClient nagerApiClient;
    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;

    @Async
    public void reSync(Integer year, String countryCode) {

        // 1. 외부 API 호출 (NagerApiClient가 이미 block() 처리해 List 반환)
        List<PublicHolidayDto> latestHolidays =
                nagerApiClient.getPublicHolidays(year, countryCode);

        // 2. fetch join 조회 (Lazy 안전)
        List<Holiday> holidays =
                holidayRepository.findAllWithCountry(year, countryCode);

        // 3. DB 변경 로직만 트랜잭션으로 위임
        saveAll(latestHolidays, holidays);

        // 디버깅이 필요할 때만 볼 수 있도록 DEBUG 레벨로 적재 결과 로그 남김
        log.debug("재동기화 완료: year={}, country={}", year, countryCode);
    }

    /**
     * DB 변경 전용 메서드
     */
    @Transactional
    protected void saveAll(List<PublicHolidayDto> latestHolidays,
                           List<Holiday> holidays) {

        //  O(N) Map 생성
        Map<String, Holiday> holidayMap =
                holidays.stream()
                        .collect(Collectors.toMap(
                                h -> h.getDate() + "_" + h.getCountryCode().getCountryCode(),
                                h -> h
                        ));

        for (PublicHolidayDto dto : latestHolidays) {

            // O(1) 조회
            Holiday existing =
                    holidayMap.get(dto.date() + "_" + dto.countryCode());

            Country country = countryRepository.findByCountryCode(dto.countryCode())
                    .orElseThrow(() ->
                            new IllegalArgumentException("존재하지 않는 국가 코드: " + dto.countryCode()));

            Holiday entity = Holiday.builder()
                    .id(existing != null ? existing.getId() : null)
                    .date(LocalDate.parse(dto.date()))
                    .localName(dto.localName())
                    .name(dto.name())
                    .countryCode(country)
                    .fixed(dto.fixed())
                    .global(dto.global())
                    .counties(dto.counties())
                    .launchYear(dto.launchYear())
                    .types(dto.types())
                    .build();

            holidayRepository.save(entity);
        }

        // 전체 Holiday 적재 정합성/성능 체크를 위한 최종 개수 로그 (INFO 레벨로 남김)
        // - 병렬 스레드 수(1,4,8,16,...)를 바꿔가며, 최종 개수가 기대값과 맞는지 빠르게 눈으로 확인하는 용도
        long totalCount = holidayRepository.count();
        log.info("Holiday 테이블 현재 전체 적재 개수: {}", totalCount);
    }
}