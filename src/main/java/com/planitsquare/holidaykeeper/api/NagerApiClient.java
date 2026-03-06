package com.planitsquare.holidaykeeper.api;

import com.planitsquare.holidaykeeper.api.dto.AvailableCountryDto;
import com.planitsquare.holidaykeeper.api.dto.PublicHolidayDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

/**
 * Nager.Date 외부 API 호출 전용 Client
 * - 모든 외부 API 연동은 이 클래스로만 수행
 * - 서비스/컨트롤러는 이 Client를 의존하여 사용
 */
@Component
@RequiredArgsConstructor
public class NagerApiClient {

    private static final Logger log = LoggerFactory.getLogger(NagerApiClient.class);

    private final WebClient nagerWebClient;

    /**
     * GET /AvailableCountries
     * 전 세계 사용 가능한 국가 목록 조회
     */
    public Flux<AvailableCountryDto> getAvailableCountries() {
        return nagerWebClient.get()
                .uri("/AvailableCountries")
                .retrieve()
                .bodyToFlux(AvailableCountryDto.class)
                .doOnError(e -> logApiError("path=/AvailableCountries", e));
    }

    /**
     * GET /PublicHolidays/{year}/{countryCode}
     * 특정 국가의 특정 연도 공휴일 조회
     */
    public Flux<PublicHolidayDto> getPublicHolidays(int year, String countryCode) {
        return nagerWebClient.get()
                .uri("/PublicHolidays/{year}/{countryCode}", year, countryCode)
                .retrieve()
                .bodyToFlux(PublicHolidayDto.class)
                .doOnError(e -> logApiError("year=" + year + ", countryCode=" + countryCode, e));
    }

    /** 에러 종류에 따라 분기 로깅 */
    private void logApiError(String context, Throwable e) {
        if (e instanceof WebClientResponseException.TooManyRequests) {
            log.warn("Nager API 429 Too Many Requests: {}", context, e);
        } else if (e instanceof WebClientRequestException) {
            log.error("Nager API 연결 실패: {}", context, e);
        } else if (e instanceof WebClientResponseException wce) {
            log.error("Nager API HTTP {} 오류: {}", wce.getStatusCode(), context, e);
        } else {
            log.error("Nager API 알 수 없는 오류: {}", context, e);
        }
    }
}
