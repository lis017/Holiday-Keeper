package com.planitsquare.holidaykeeper.holiday.service;

import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
    필터검색시 db내용 바로 응답
 */
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidaySyncService holidaySyncService;

    public Page<PublicHolidayDto> search(Integer year, String countryCode, Pageable pageable) {

        // 1. DB에서 먼저 조회 → 클라이언트 즉시 응답
        Page<PublicHolidayDto> page = holidayRepository.search(year, countryCode, pageable)
                .map(this::toDto);

        // 2. 백그라운드 비동기 재동기화 호출
        holidaySyncService.reSync(year, countryCode);

        return page;
    }
    /** 특정 연도·국가 공휴일 전체 삭제 */
    @Transactional
    public void deleteByYearAndCountryCode(Integer year, String countryCode) {
        holidayRepository.deleteByYearAndCountryCode(year, countryCode);
    }

    public void saveAll(List<Holiday> holidays) {
        holidayRepository.saveAll(holidays);
    }

    private PublicHolidayDto toDto(Holiday h) {
        return new PublicHolidayDto(
                h.getDate().toString(),
                h.getLocalName(),
                h.getName(),
                h.getCountryCode().getCountryCode(),
                h.isFixed(),
                h.isGlobal(),
                h.getCounties(),
                h.getLaunchYear(),
                h.getTypes()
        );
    }
}
