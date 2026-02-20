package com.planitsquare.holidaykeeper.holiday.mapper;

import com.planitsquare.holidaykeeper.holiday.entity.Holiday;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HolidayMapper {

    List<Holiday> findByYearAndCountryCode(
            @Param("year") Integer year,
            @Param("countryCode") String countryCode
    );

    void deleteByYearAndCountryCode(
            @Param("year") Integer year,
            @Param("countryCode") String countryCode
    );

    int upsertHoliday(@Param("holiday") Holiday holiday);
}
