package com.planitsquare.holidaykeeper.holiday;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HolidayRepositoryCustom {
    Page<Holiday> search(Integer year, String countryCode, Pageable pageable);
}