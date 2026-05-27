package com.revy.example.saas.config;

import com.revy.example.jwt.JwtAuthenticationFilter;
import com.revy.example.security.SecurityExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private static final String[] ALLOW_LIST = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api/v1/meta/codes",
        "/api/v1/auth/signup",
        "/api/v1/auth/login",
        "/api/v1/fx/currencies",
        "/api/v1/fx/rate/latest",
        "/api/v1/insurance/products",
        "/api/v1/insurance/products/**",
        "/actuator/health"};

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   SecurityExceptionHandler securityExceptionHandler) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.requestMatchers(ALLOW_LIST)
                .permitAll()
                .anyRequest()
                .authenticated())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(securityExceptionHandler)
                .accessDeniedHandler(securityExceptionHandler))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
