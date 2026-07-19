package com.revy.resource.config;


import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 리소스 서버의 인증/인가 정책을 구성하는 보안 설정 클래스입니다.
 *
 * <p>Keycloak에서 발급한 JWT를 검증하고, 토큰의 realm role을 Spring Security 권한으로 변환하여
 * API 경로별 접근 제어에 사용합니다.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * HTTP 요청에 적용할 Spring Security 필터 체인을 구성합니다.
     *
     * <p>헬스 체크와 공개 API는 인증 없이 허용하고, 사용자/관리자 API는 JWT에서 변환된 역할 기반 권한으로
     * 접근을 제한합니다.</p>
     *
     * @param http Spring Security HTTP 보안 설정 빌더
     * @return 애플리케이션에 적용할 보안 필터 체인
     * @throws Exception 보안 필터 체인 생성 중 오류가 발생한 경우
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authorize -> authorize
                                       .requestMatchers("/actuator/health", "/api/public").permitAll()
                                       .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                       .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                                       .anyRequest().authenticated()
                                  )
            .oauth2ResourceServer(oauth2 -> oauth2
                                      .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                 )
            .cors(Customizer.withDefaults())
            .build();
    }

    /**
     * JWT 인증 결과를 Spring Security Authentication으로 변환하는 컨버터를 생성합니다.
     *
     * <p>Keycloak의 `realm_access.roles` 클레임을 `ROLE_` 접두사의 권한으로 변환하고,
     * `preferred_username` 클레임을 인증 주체 이름으로 사용합니다.</p>
     *
     * @return Keycloak JWT 클레임을 반영하는 JWT 인증 컨버터
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtRolesConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    /**
     * JWT 서명, 발급자, 대상자 검증을 수행하는 디코더를 생성합니다.
     *
     * <p>issuer-uri의 OpenID Provider 메타데이터에서 JWK Set 정보를 조회해 서명을 검증하고,
     * 기본 발급자 검증에 더해 `resource-api` audience가 포함된 토큰만 허용합니다.</p>
     *
     * @param issuerUri Keycloak Realm의 issuer URI
     * @return 리소스 서버에서 사용할 JWT 디코더
     */
    @Bean
    JwtDecoder jwtDecoder(
        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        String issuerUri
                         ) {
        NimbusJwtDecoder decoder =
            (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> issuerValidator =
            JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator =
            new AudienceValidator("resource-api");

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));

        return decoder;
    }
}
