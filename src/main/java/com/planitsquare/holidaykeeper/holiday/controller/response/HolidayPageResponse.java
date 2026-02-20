package com.planitsquare.holidaykeeper.holiday.controller.response;

import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import java.util.List;

//// Page 객체를 직접 노출하지 않고, 캐시 및 API 응답을 위해 사용하는 Page 응답 DTO
public record HolidayPageResponse(
        List<PublicHolidayDto> content,
        int page,
        int size,
        long totalElements
) {}