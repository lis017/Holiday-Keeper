package com.planitsquare.holidaykeeper.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiTest {

    @Autowired
    NagerApiClient client;

    @Test
    void testCountries() {
        System.out.println(client.getAvailableCountries());
    }

    @Test
    void testHolidays() {
        System.out.println(client.getPublicHolidays(2025, "KR"));
    }
}