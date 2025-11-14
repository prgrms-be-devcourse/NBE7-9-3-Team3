package org.example.backend.global.springdoc

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = io.swagger.v3.oas.annotations.info.Info(
        title = "NBE7-9-2-Team3 어항관리 API",
        description = "NBE7-9-2-Team3 어항관리 API 문서"
    ), security = [SecurityRequirement(name = "bearerAuth")]
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("NBE7-9-2-Team3 API")
                    .version("1.0.0")
                    .description("NBE7-9-2-Team3 프로젝트의 REST API 문서입니다.")
            )
    }

    @Bean
    fun memberApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("01. Member API")
            .pathsToMatch("/api/members/**")
            .build()
    }

    @Bean
    fun followApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("02. Follow API")
            .pathsToMatch("/api/follows/**")
            .build()
    }

    @Bean
    fun fishApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("03. Fish API")
            .pathsToMatch(
                "/api/aquarium/*/fish/**",
                "/api/fish/*/fishLog/**"
            )
            .build()
    }

    @Bean
    fun aquariumApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("04. Aquarium API")
            .pathsToMatch(
                "/api/aquarium/**",
                "/api/aquarium/*/aquariumLog/**"
            )
            .pathsToExclude("/api/aquarium/*/fish/**")
            .build()
    }

    @Bean
    fun tradeApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("05. Trade API")
            .pathsToMatch("/api/market/**")
            .build()
    }

    @Bean
    fun postApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("06. Post API")
            .pathsToMatch("/api/posts/**")
            .build()
    }

    @Bean
    fun pointApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("07. Point API")
            .pathsToMatch("/api/points/**")
            .build()
    }

    @Bean
    fun tradeChatApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("08. Trade Chat API")
            .pathsToMatch("/api/chat/**")
            .build()
    }
}