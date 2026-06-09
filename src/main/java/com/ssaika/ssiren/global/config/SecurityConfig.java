package com.ssaika.ssiren.global.config;

import com.ssaika.ssiren.global.exception.ErrorCode;
import com.ssaika.ssiren.global.security.JwtAuthenticationFilter;
import com.ssaika.ssiren.global.security.SecurityErrorWriter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorWriter securityErrorWriter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    securityErrorWriter.writeErrorResponse(response, ErrorCode.UNAUTHORIZED))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    securityErrorWriter.writeErrorResponse(response, ErrorCode.FORBIDDEN)))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/token/refresh").permitAll()
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .requestMatchers("/api/v1/reports").authenticated()
                .requestMatchers("/api/v1/reports/**").authenticated()
                .requestMatchers("/api/v1/issues").authenticated()
                .requestMatchers("/api/v1/issues/**").authenticated()
                .requestMatchers("/api/v1/chatbots").authenticated()
                .requestMatchers("/api/v1/chatbots/**").authenticated()
                // TODO: 추후 리포트 관련 api 설정 추가하며 반영 고려
                .requestMatchers("/api/v1/reports/drafts").authenticated()
                .requestMatchers("/api/v1/reports").authenticated()
                // TODO: 로그인 연동 후 authenticated()로 복구
                // .requestMatchers("/api/v1/reports/me").authenticated()
                .requestMatchers("/api/v1/users/me/**").authenticated()
                .requestMatchers("/api/v1/users/me").authenticated()
                .requestMatchers("/api/v1/notifications/tokens").authenticated()
                .requestMatchers("/api/v1/admin/**").authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
