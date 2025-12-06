package com.planitsquare.holidaykeeper.holiday;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByCountry_CountryCodeAndDate(String countryCode, LocalDate date);
}