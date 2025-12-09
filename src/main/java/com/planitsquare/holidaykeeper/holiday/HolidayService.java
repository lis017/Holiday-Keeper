package com.planitsquare.holidaykeeper.holiday;

import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;

    /** DB에 Holiday 저장 */
    public void saveAll(List<Holiday> holidays) {
        holidayRepository.saveAll(holidays);
    }

    /**
     * Holiday 엔티티 검색 서비스.
     */
    public Page<PublicHolidayDto> search(Integer year, String countryCode, Pageable pageable) {
        return holidayRepository.search(year, countryCode, pageable)
                .map(this::toDto);
    }
    /** * Holiday 엔티티 → PublicHolidayDto 로 변환하는 Mapper 메서드.
     *  * 엔티티 전체를 API 응답으로 직접 노출하지 않고,
     *  * 외부 API DTO를 재사용해 중복을 줄이고 계층 분리를 강화한 설계입니다 */
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