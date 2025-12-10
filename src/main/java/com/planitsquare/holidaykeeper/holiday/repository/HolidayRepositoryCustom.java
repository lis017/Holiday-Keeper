package com.planitsquare.holidaykeeper.holiday.repository;

import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HolidayRepositoryCustom {
    Page<Holiday> search(Integer year, String countryCode, Pageable pageable);
    List<Holiday> findByYearAndCountryCode(Integer year, String countryCode);
    void deleteByYearAndCountryCode(Integer year, String countryCode);
}