package com.planitsquare.holidaykeeper.holiday;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long>, HolidayRepositoryCustom {

}