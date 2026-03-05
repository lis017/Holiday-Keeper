package com.planitsquare.holidaykeeper.holiday.service;

import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.holiday.controller.response.HolidayPageResponse;
import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import com.planitsquare.holidaykeeper.holiday.mapper.HolidayMapper;
import com.planitsquare.holidaykeeper.holiday.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private final HolidayRepository holidayRepository;  //JPA
    private final HolidayMapper holidayMapper;          //MyBatis
    private final HolidaySyncService holidaySyncService;

    /**
     * 🔹 캐시 전용 메서드
     * - PageImpl 캐싱 x
     * - DTO List만 캐싱 o
     */
    @Cacheable(
            cacheNames = "holidaySearch",
            key = "'holiday:' + #year + ':' + #countryCode + ':' + " +
                    "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + " +
                    "#pageable.sort.toString()"
    )
    public List<PublicHolidayDto> searchForCache(
            Integer year,
            String countryCode,
            Pageable pageable
    ) {
        return holidayRepository
                .search(year, countryCode, pageable)
                .map(this::toDtoWithInitializedTypes)
                .getContent();
    }

    /**
     * 🔹 외부 API 응답용 메서드
     * - 캐시 조회 후 Page 정보 재조립
     */
    public HolidayPageResponse search(
            Integer year,
            String countryCode,
            Pageable pageable
    ) {
        List<PublicHolidayDto> content =
                searchForCache(year, countryCode, pageable);

        long totalElements =
                holidayRepository.countByYearAndCountryCode_CountryCode(year, countryCode);

        holidaySyncService.reSync(year, countryCode);

        return new HolidayPageResponse(
                content,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                totalElements
        );
    }

    /**
     * 특정 연도·국가 공휴일 전체 삭제
     */
    @CacheEvict(
            cacheNames = "holiday",
            allEntries = true
    )
    @Transactional
    public void deleteByYearAndCountryCode(Integer year, String countryCode) {
        holidayRepository.deleteByYearAndCountryCode(year, countryCode);
    }
    @Transactional
    public void saveAll(List<Holiday> holidays) {
        holidayRepository.saveAll(holidays);
    }

    /**
     * DTO 변환 + Lazy 컬렉션 초기화
     */
    private PublicHolidayDto toDtoWithInitializedTypes(Holiday h) {

        List<String> types = h.getTypes();

        return new PublicHolidayDto(
                h.getDate().toString(),
                h.getLocalName(),
                h.getName(),
                h.getCountryCode().getCountryCode(),
                h.isFixed(),
                h.isGlobal(),
                h.getCounties(),
                h.getLaunchYear(),
                types
        );
    }
}