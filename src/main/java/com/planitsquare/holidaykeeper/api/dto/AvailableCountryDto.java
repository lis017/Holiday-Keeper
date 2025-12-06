package com.planitsquare.holidaykeeper.api.dto;

/**
 * /AvailableCountries 응답 DTO
 */
public record AvailableCountryDto(
        String countryCode,
        String name
) {}