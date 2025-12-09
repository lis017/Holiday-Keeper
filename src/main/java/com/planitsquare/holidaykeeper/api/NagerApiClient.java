package com.planitsquare.holidaykeeper.api;

import com.planitsquare.holidaykeeper.api.dto.AvailableCountryDto;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

/**
 * Nager.Date 외부 API 호출 전용 Client
 * - 모든 외부 API 연동은 이 클래스로만 수행
 * - 서비스/컨트롤러는 이 Client를 의존하여 사용
 */
@Component
@RequiredArgsConstructor
public class NagerApiClient {

    private final WebClient nagerWebClient;

    /**
     * GET /AvailableCountries
     * 전 세계 사용 가능한 국가 목록 조회
     */
    public List<AvailableCountryDto> getAvailableCountries() {
        return nagerWebClient.get()
                .uri("/AvailableCountries")
                .retrieve()
                .bodyToFlux(AvailableCountryDto.class)
                .collectList()
                .block();
    }

    /**
     * GET /PublicHolidays/{year}/{countryCode}
     * 특정 국가의 특정 연도 공휴일 조회
     */
    public List<PublicHolidayDto> getPublicHolidays(int year, String countryCode) {
        return nagerWebClient.get()
                .uri("/PublicHolidays/{year}/{countryCode}", year, countryCode)
                .retrieve()
                .bodyToFlux(PublicHolidayDto.class)
                .collectList()
                .block();
    }
}