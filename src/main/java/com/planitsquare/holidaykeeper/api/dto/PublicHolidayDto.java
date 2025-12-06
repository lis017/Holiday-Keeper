package com.planitsquare.holidaykeeper.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * /PublicHolidays/{year}/{countryCode} 응답 DTO
 */
public record PublicHolidayDto(
        String date,
        String localName,
        String name,
        String countryCode,
        boolean fixed,
        boolean global,
        List<String> counties,
        Integer launchYear,
        List<String> types   // ← 여기! String → List<String> 으로 수정
) {}