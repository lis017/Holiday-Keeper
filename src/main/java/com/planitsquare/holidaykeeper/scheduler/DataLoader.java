//package com.planitsquare.holidaykeeper.scheduler;
//
//import com.planitsquare.holidaykeeper.api.NagerApiClient;
//import com.planitsquare.holidaykeeper.api.dto.AvailableCountryDto;
//import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
//import com.planitsquare.holidaykeeper.country.Country;
//import com.planitsquare.holidaykeeper.country.CountryService;
//import com.planitsquare.holidaykeeper.holiday.Holiday;
//import com.planitsquare.holidaykeeper.holiday.HolidayService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 외부 API 기반 초기 데이터 적재
// */
//@Component
//@RequiredArgsConstructor
//public class DataLoader implements CommandLineRunner {
//
//    private final NagerApiClient nagerApiClient;
//    private final CountryService countryService;
//    private final HolidayService holidayService;
//
//    @Override
//    public void run(String... args) throws Exception {
//        loadCountriesFromApi();
//        loadHolidaysFromApi();
//    }
//
//    /** 외부 API 기반 국가 데이터 적재 */
//    private void loadCountriesFromApi() {
//        List<AvailableCountryDto> countryDtos = nagerApiClient.getAvailableCountries();
//        List<Country> countries = new ArrayList<>();
//        for (AvailableCountryDto dto : countryDtos) {
//            countries.add(Country.builder()
//                    .countryCode(dto.countryCode())
//                    .name(dto.name())
//                    .build());
//        }
//        countryService.saveAll(countries);
//        System.out.println("외부 API 기반 Country 데이터 적재 완료: " + countries.size() + "개");
//    }
//
//    /** 최근 5~6년 공휴일 데이터 외부 API에서 적재 */
//    private void loadHolidaysFromApi() {
//        List<Country> countries = countryService.findAll();
//        List<Holiday> holidays = new ArrayList<>();
//        int startYear = 2020;
//        int endYear = 2025;
//
//        for (Country country : countries) {
//            for (int year = startYear; year <= endYear; year++) {
//                List<PublicHolidayDto> holidayDtos = nagerApiClient.getPublicHolidays(year, country.getCountryCode());
//                for (PublicHolidayDto dto : holidayDtos) {
//                    holidays.add(Holiday.builder()
//                            .country(country)
//                            .date(LocalDate.parse(dto.date()))
//                            .localName(dto.localName())
//                            .name(dto.name())
//                            .year(year)
//                            .global(dto.global())
//                            .build());
//                }
//            }
//        }
//
//        holidayService.saveAll(holidays);
//        System.out.println("외부 API 기반 Holiday 데이터 적재 완료: " + holidays.size() + "개");
//    }
//}