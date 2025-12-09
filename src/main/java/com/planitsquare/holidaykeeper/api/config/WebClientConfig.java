package com.planitsquare.holidaykeeper.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
@Configuration
public class WebClientConfig {

    /**
     * Nager.Date API 전용 WebClient 설정
     * - baseUrl: 외부 API 주소
     * - timeout: 5초
     * - 대용량 응답 처리 위해 buffer size 확장
     */
    @Bean
    public WebClient nagerWebClient() {
        return WebClient.builder()
                .baseUrl("https://date.nager.at/api/v3")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }
}