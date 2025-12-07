package com.planitsquare.holidaykeeper.holiday;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;

    /** DB에 Holiday 저장 */
    public void saveAll(List<Holiday> holidays) {
        holidayRepository.saveAll(holidays);
    }

    public List<Holiday> findByCountryIdAndYear(Long countryId, int year) {
        return holidayRepository.findByCountryIdAndYear(countryId, year);
    }
}