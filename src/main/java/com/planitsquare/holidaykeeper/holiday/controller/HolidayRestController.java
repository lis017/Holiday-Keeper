package com.planitsquare.holidaykeeper.holiday.controller;

import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import com.planitsquare.holidaykeeper.holiday.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/holiday")
@RequiredArgsConstructor
public class HolidayRestController {

    private final HolidayService holidayService;

    @Operation(summary = "연도별 국가별 공휴일 조회",
            description = "연도와 국가 ID를 기반으로 공휴일을 조회하고 페이징 처리합니다.")
    @GetMapping
    public Page<PublicHolidayDto> getHolidays(
            @Parameter(description = "조회할 연도") @RequestParam(required = false) Integer year,
            @Parameter(description = "조회할 국가 ID") @RequestParam(required = false) String countryCode,
            Pageable pageable
    ) {
        return holidayService.search(year, countryCode, pageable);
    }
}
