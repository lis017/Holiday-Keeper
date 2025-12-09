package com.planitsquare.holidaykeeper.holiday.service;

import com.planitsquare.holidaykeeper.api.NagerApiClient;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.country.Country;
import com.planitsquare.holidaykeeper.country.CountryRepository;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;

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
    public void reSync(Integer year, String countryCode) {
        // 1. 외부 API에서 최신 데이터 조회
        List<PublicHolidayDto> latestHolidays = nagerApiClient.getPublicHolidays(year, countryCode);

        // 2. DB에서 기존 데이터 조회
        List<Holiday> existingHolidays = holidayRepository.findAll(); // 필요시 필터링 추가 가능

        // 3. 비교 후 Upsert 수행
        for (PublicHolidayDto dto : latestHolidays) {
            // 예: date+countryCode 기준으로 기존 Holiday 찾기
            Holiday existing = existingHolidays.stream()
                    .filter(h -> h.getDate().toString().equals(dto.date())
                            && h.getCountryCode().getCountryCode().equals(dto.countryCode()))
                    .findFirst()
                    .orElse(null);

            Country country = countryRepository.findByCountryCode(dto.countryCode())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 국가 코드: " + dto.countryCode()));

            Holiday entity = Holiday.builder()
                    .id(existing != null ? existing.getId() : null) // 기존 ID 유지
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

            holidayRepository.save(entity); // INSERT or UPDATE 자동 처리

        }

        System.out.println("재동기화 완료: year=" + year + ", country=" + countryCode);
    }
}