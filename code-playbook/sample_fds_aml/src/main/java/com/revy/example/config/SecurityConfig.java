package com.revy.example.config;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/transactions").hasRole("TRANSACTION_PROCESSOR")
                .requestMatchers("/api/v1/fds/alerts/**").hasRole("FDS_ANALYST")
                .requestMatchers("/api/v1/aml/cases/**").hasRole("AML_OFFICER")
                .anyRequest().authenticated()
            )
            .build();
    }
}