package com.planitsquare.holidaykeeper.country;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
    // countryCode로 조회하는 메서드 예시
    boolean existsByCountryCode(String countryCode);
}