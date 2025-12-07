package com.planitsquare.holidaykeeper.holiday;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HolidayRepositoryTest {

    @Autowired
    HolidayRepository holidayRepository;

    @Test
    void 데이터가_정상적으로_적재되었는지_확인() {
        int count = holidayRepository.findAll().size();
        System.out.println("HOLIDAY 개수 = " + count);

        assertThat(count).isGreaterThan(0);
    }
}