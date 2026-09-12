package com.revy.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchange -> exchange
                                   .pathMatchers(
                                       "/actuator/health",
                                       "/oauth2/**",
                                       "/login/**"
                                                ).permitAll()
                                   .anyExchange().authenticated()
                              )
            .oauth2Login(Customizer.withDefaults())
            .oauth2Client(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .build();
    }
}
