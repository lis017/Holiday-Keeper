package com.planitsquare.holidaykeeper.holiday.service;

import com.planitsquare.holidaykeeper.api.NagerApiClient;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.country.Country;
import com.planitsquare.holidaykeeper.country.CountryRepository;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    private final NagerApiClient nagerApiClient;
    private final HolidayRepository holidayRepository;
    private final CountryRepository countryRepository;

    @Async
    @CacheEvict(
            cacheNames = "holidaySearch",
            allEntries = true
    )
    public void reSync(Integer year, String countryCode) {

        // 1. 외부 API 호출
        List<PublicHolidayDto> latestHolidays =
                nagerApiClient.getPublicHolidays(year, countryCode);

        // 2. fetch join 조회 (Lazy 안전)
        List<Holiday> holidays =
                holidayRepository.findAllWithCountry(year, countryCode);

        // 3. DB 변경 로직만 트랜잭션으로 위임
        saveAll(latestHolidays, holidays);

        System.out.println("재동기화 완료: year=" + year + ", country=" + countryCode);
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
    }
}