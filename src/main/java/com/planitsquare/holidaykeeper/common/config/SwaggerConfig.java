package com.planitsquare.holidaykeeper.common.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Holiday API")
                        .description("공휴일 조회 API")
                        .version("v1"));
    }

    /**
     * Pageable에 대한 Swagger 설정
     * page / size / sort 파라미터가 자동으로 Swagger UI에 나타나도록 한다.
     */
    @Bean
    public OpenApiCustomizer swaggerPageableCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {

                    operation.addParametersItem(
                            new Parameter()
                                    .in(ParameterIn.QUERY.toString())
                                    .name("page")
                                    .description("조회할 페이지 번호 (0부터 시작)")
                                    .required(false)
                    );

                    operation.addParametersItem(
                            new Parameter()
                                    .in(ParameterIn.QUERY.toString())
                                    .name("size")
                                    .description("페이지당 항목 수")
                                    .required(false)
                    );

                    operation.addParametersItem(
                            new Parameter()
                                    .in(ParameterIn.QUERY.toString())
                                    .name("sort")
                                    .description("정렬 기준, 예: name,asc")
                                    .required(false)
                    );

                })
        );
    }
}
