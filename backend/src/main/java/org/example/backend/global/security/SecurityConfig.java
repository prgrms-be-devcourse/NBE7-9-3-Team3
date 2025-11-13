package org.example.backend.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationFilter customAuthenticationFilter;

    public SecurityConfig(CustomAuthenticationFilter customAuthenticationFilter) {
        this.customAuthenticationFilter = customAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // H2 Console을 위한 설정
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // H2 Console이 iframe을 사용하므로 허용
        );

        // 인증/인가 설정
        http.authorizeHttpRequests(authorize -> authorize
                // H2 Console 경로 허용
                .requestMatchers("/h2-console/**").permitAll()
                // 회원가입, 로그인, 로그아웃 경로 허용
                .requestMatchers("/api/members/join", "/api/members/login", "/api/members/logout").permitAll()
                // WebSocket 경로 허용
                .requestMatchers("/ws/**").permitAll()
                // Swagger UI 경로 허용 (정적 리소스 포함)
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html", "/webjars/**", "/v3/api-docs/**").permitAll()
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // 커스텀 인증 필터 추가
        http.addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 폼 로그인 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);

        // HTTP Basic 인증 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);

        // CORS 설정 활성화 (spring security는 기본적으로 cors 차단)
        http.cors(cors -> {});

        return http.build();
    }
}
