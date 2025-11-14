package org.example.backend.global.springdoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "NBE7-9-2-Team3 어항관리 API",
        description = "NBE7-9-2-Team3 어항관리 API 문서"
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new io.swagger.v3.oas.models.info.Info()
                .title("NBE7-9-2-Team3 API")
                .version("1.0.0")
                .description("NBE7-9-2-Team3 프로젝트의 REST API 문서입니다.")
            );
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
            .group("01. Member API")
            .pathsToMatch("/api/members/**")
            .build();
    }

    @Bean
    public GroupedOpenApi followApi() {
        return GroupedOpenApi.builder()
            .group("02. Follow API")
            .pathsToMatch("/api/follows/**")
            .build();
    }

    @Bean
    public GroupedOpenApi fishApi() {
        return GroupedOpenApi.builder()
            .group("03. Fish API")
            .pathsToMatch(
                "/api/aquarium/*/fish/**",
                "/api/fish/*/fishLog/**"
                )
            .build();
    }

    @Bean
    public GroupedOpenApi aquariumApi() {
        return GroupedOpenApi.builder()
            .group("04. Aquarium API")
            .pathsToMatch(
                "/api/aquarium/**",
                "/api/aquarium/*/aquariumLog/**"
                )
            .pathsToExclude("/api/aquarium/*/fish/**")
            .build();
    }

    @Bean
    public GroupedOpenApi tradeApi() {
        return GroupedOpenApi.builder()
            .group("05. Trade API")
            .pathsToMatch("/api/market/**")
            .build();
    }

    @Bean
    public GroupedOpenApi postApi() {
        return GroupedOpenApi.builder()
            .group("06. Post API")
            .pathsToMatch("/api/posts/**")
            .build();
    }

    @Bean
    public GroupedOpenApi pointApi() {
        return GroupedOpenApi.builder()
            .group("07. Point API")
            .pathsToMatch("/api/points/**")
            .build();
    }

    @Bean
    public GroupedOpenApi tradeChatApi() {
        return GroupedOpenApi.builder()
            .group("08. Trade Chat API")
            .pathsToMatch("/api/chat/**")
            .build();
    }

}