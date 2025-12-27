package com.revy.admin.security;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig  {

    private final AdminServerProperties adminServer;


    public SecurityConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // 1. 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                adminServer.path("/login"),
                                adminServer.path("/assets/**"),
                                adminServer.path("/sba-settings.js")
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 2. Form Login
                .formLogin(form -> form
                        .loginPage(adminServer.path("/login"))
                        //.successHandler(loginSuccessHandler)
                        .permitAll()
                )

                // 3. Logout
                .logout(logout -> logout
                        .logoutUrl(adminServer.path("/logout"))
                )

                // 4. Client 등록용 HTTP Basic
                .httpBasic(Customizer.withDefaults())

                // 5. CSRF 설정
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository.withHttpOnlyFalse()
                        )
                        .ignoringRequestMatchers(
                                adminServer.path("/instances"),
                                adminServer.path("/actuator/**")
                        )
                );

        return http.build();
    }


}
