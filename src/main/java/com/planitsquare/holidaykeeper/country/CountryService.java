package com.planitsquare.holidaykeeper.country;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    /** DB에 Country 저장 */
    public void saveAll(List<Country> countries) {
        countryRepository.saveAll(countries);
    }

    /** country 데이터 가져오기 */
    public List<Country> findAll() {
        return countryRepository.findAll();
    }
}