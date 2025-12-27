package com.revy.admin.security;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/instances", "/actuator/**")
//                        .permitAll()  // 클라이언트 등록 허용
//                        .anyRequest()
//                        .authenticated()
//                );
//
//        return http.build();
//    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring().requestMatchers("/static/**");
//    }
}
