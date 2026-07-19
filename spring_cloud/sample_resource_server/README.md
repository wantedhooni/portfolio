# Spring Cloud Gateway와 Keycloak으로 구성하는 Resource Server 보안 구조

이 프로젝트는 Spring Cloud Gateway가 OAuth2 로그인 진입점 역할을 하고, `resource-service`가 Keycloak에서 발급한 JWT를 검증하는 Resource Server 역할을 맡는 샘플입니다. 핵심은 게이트웨이에서 로그인한 사용자의 access token을 downstream API로 전달하고, 리소스 서버가 그 토큰을 독립적으로 검증한 뒤 역할 기반으로 API 접근을 제어하는 구조입니다.

## 프로젝트 구조

```text
sample_resource_server
├── api-gateway
│   └── Spring Cloud Gateway + OAuth2 Client
├── auth-server
│   └── 인증 서버 실험용 모듈
├── resource-service
│   └── OAuth2 Resource Server + JWT 검증
├── docker-compose-infra.yml
│   └── Keycloak 등 인프라 실행 구성
└── keycloak.json
    └── Keycloak Realm 설정 예시
```

루트 Gradle 설정은 멀티 모듈 구조를 사용합니다. 공통적으로 Java 21 toolchain, Spring Boot BOM, Spring Cloud BOM을 적용하고, 각 서비스 모듈은 필요한 Spring Boot starter를 개별 선언합니다.

## 전체 인증 흐름

1. 사용자가 `api-gateway`의 보호된 경로에 접근합니다.
2. Gateway는 OAuth2 Authorization Code Flow로 Keycloak 로그인을 유도합니다.
3. 로그인 성공 후 Gateway는 사용자 세션과 OAuth2 client 정보를 보관합니다.
4. `/api/**` 요청이 들어오면 Gateway의 `TokenRelay` 필터가 access token을 `Authorization: Bearer ...` 헤더로 전달합니다.
5. `resource-service`는 전달받은 JWT의 서명, 발급자, audience를 검증합니다.
6. 검증된 JWT의 Keycloak role을 Spring Security authority로 변환합니다.
7. API 경로별 정책에 따라 접근 허용 여부를 결정합니다.

이 구조에서는 Gateway와 Resource Server의 책임을 분리합니다. Gateway는 사용자 로그인과 토큰 전달에 집중하고, Resource Server는 전달받은 토큰을 신뢰하지 않고 직접 검증합니다. 운영 환경에서도 각 API 서비스가 자기 보호 경계를 가지므로 서비스 간 책임이 명확합니다.

## Resource Server 핵심 설정

분석 대상 파일은 `resource-service/src/main/java/com/revy/resource/config/SecurityConfig.java`입니다.

### 1. API 경로별 접근 제어

```java
.authorizeHttpRequests(authorize -> authorize
    .requestMatchers("/actuator/health", "/api/public").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
    .anyRequest().authenticated()
)
```

보안 정책은 공개 API, 관리자 API, 사용자 API, 기타 인증 필요 API로 구분됩니다.

- `/actuator/health`, `/api/public`: 인증 없이 접근 가능합니다.
- `/api/admin/**`: `ROLE_ADMIN` 권한이 필요합니다.
- `/api/user/**`: `ROLE_USER` 또는 `ROLE_ADMIN` 권한이 필요합니다.
- 그 외 모든 요청: 유효한 인증이 필요합니다.

Spring Security의 `hasRole("ADMIN")`은 내부적으로 `ROLE_ADMIN` authority를 찾습니다. 그래서 JWT role을 변환할 때 `ROLE_` 접두사를 붙이는 과정이 중요합니다.

### 2. JWT 기반 Resource Server 활성화

```java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
)
```

리소스 서버는 세션 기반 인증이 아니라 Bearer Token 기반 인증을 사용합니다. 요청에 포함된 JWT가 유효하면 Spring Security가 `Authentication` 객체를 만들고, 이후 컨트롤러와 인가 정책에서 인증 정보를 사용할 수 있습니다.

이 프로젝트의 `AuthController`는 `/api/user/me`에서 `Authentication`과 `Jwt`를 함께 받아 principal, subject, issuer, authorities, claims를 응답합니다. 실무에서는 디버깅용으로 유용하지만, 운영 API에서 전체 claims를 그대로 노출하는 것은 피해야 합니다.

### 3. Keycloak Role을 Spring Authority로 변환

```java
JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtRolesConverter());
converter.setPrincipalClaimName("preferred_username");
```

Keycloak은 Realm Role을 일반적으로 다음과 같은 클레임에 담습니다.

```json
{
  "preferred_username": "revy",
  "realm_access": {
    "roles": ["USER", "ADMIN"]
  }
}
```

`KeycloakJwtRolesConverter`는 `realm_access.roles` 값을 읽고 각각을 `ROLE_USER`, `ROLE_ADMIN` 같은 Spring Security authority로 변환합니다. 이 변환이 있어야 `hasRole("ADMIN")`, `hasAnyRole("USER", "ADMIN")` 같은 선언형 인가가 정상 동작합니다.

또한 `preferred_username`을 principal claim으로 지정했기 때문에 `authentication.getName()`은 JWT subject가 아니라 Keycloak 사용자명 기준으로 반환됩니다. 로그, 감사 추적, 응답 메시지에서 사람이 읽기 쉬운 식별자를 사용할 수 있다는 장점이 있습니다.

### 4. issuer와 audience 검증

```java
NimbusJwtDecoder decoder =
    (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);

OAuth2TokenValidator<Jwt> issuerValidator =
    JwtValidators.createDefaultWithIssuer(issuerUri);

OAuth2TokenValidator<Jwt> audienceValidator =
    new AudienceValidator("resource-api");

decoder.setJwtValidator(
    new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator)
);
```

JWT 검증은 단순히 서명만 확인해서는 부족합니다. 이 프로젝트는 두 가지 검증을 명시합니다.

- `issuer` 검증: 신뢰하는 Keycloak Realm이 발급한 토큰인지 확인합니다.
- `audience` 검증: 이 API를 대상으로 발급된 토큰인지 확인합니다.

`issuer-uri`는 `resource-service/src/main/resources/application.yml`에 정의되어 있습니다.

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:9090/realms/demo}
```

`JwtDecoders.fromIssuerLocation(issuerUri)`는 OpenID Provider 메타데이터를 조회하고 JWK Set URI를 찾아 공개키 기반 서명 검증을 구성합니다. 별도로 공개키를 애플리케이션에 하드코딩하지 않아도 되는 방식입니다.

audience는 `AudienceValidator("resource-api")`로 고정되어 있습니다. 운영에서는 서비스별 audience를 명확히 나누고, 환경 변수나 설정 프로퍼티로 분리하는 것이 관리에 유리합니다.

## Gateway 설정과의 연결

`api-gateway/src/main/resources/application.yml`의 핵심은 OAuth2 client와 `TokenRelay`입니다.

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: resource-service
              uri: ${RESOURCE_SERVICE_URI:http://localhost:8081}
              predicates:
                - Path=/api/**
              filters:
                - TokenRelay=
```

Gateway는 `/api/**` 요청을 `resource-service`로 라우팅하고, 로그인한 사용자의 access token을 전달합니다. Resource Server는 Gateway가 전달했다는 사실만으로 요청을 신뢰하지 않고, JWT 자체를 다시 검증합니다. 이 방식은 Gateway 우회 접근이나 잘못된 토큰 전달에 대한 방어선을 제공합니다.

## API별 기대 동작

| 경로 | 인증 필요 | 필요 권한 | 설명 |
| --- | --- | --- | --- |
| `/actuator/health` | 아니오 | 없음 | 헬스 체크 |
| `/api/public` | 아니오 | 없음 | 공개 API |
| `/api/user/me` | 예 | `USER` 또는 `ADMIN` | 현재 사용자 정보 확인 |
| `/api/admin/report` | 예 | `ADMIN` | 관리자 API |
| 기타 경로 | 예 | 유효한 JWT | 기본 보호 정책 |

## 실무 관점의 보안 포인트

### audience 검증은 반드시 유지한다

서명과 issuer가 유효한 토큰이라도, 다른 클라이언트나 다른 API를 대상으로 발급된 토큰일 수 있습니다. audience 검증은 토큰 재사용 범위를 줄이는 핵심 방어선입니다.

### role 매핑 규칙을 명확히 관리한다

Keycloak role 이름과 Spring Security authority 이름 사이에는 `ROLE_` 접두사 규칙이 있습니다. role 이름을 `USER`, `ADMIN`처럼 대문자 기준으로 관리하면 `hasRole` 정책과의 매핑이 단순해집니다.

### 운영 API에서 claims 전체 노출은 피한다

샘플의 `/api/user/me`는 학습과 검증을 위해 JWT claims를 그대로 반환합니다. 운영에서는 필요한 필드만 선별해서 반환해야 하며, 토큰 내부 구조가 외부에 노출되지 않도록 응답 DTO를 분리하는 편이 안전합니다.

### client secret 기본값은 운영에서 제거한다

Gateway 설정에는 개발 편의를 위한 기본 client secret 값이 있습니다. 운영에서는 환경 변수 또는 Secret Manager를 통해 주입하고, 설정 파일에 기본 secret을 남기지 않는 것이 안전합니다.

## 실행 전 확인 사항

- Keycloak Realm issuer가 `KEYCLOAK_ISSUER_URI`와 일치해야 합니다.
- access token의 `aud` 클레임에 `resource-api`가 포함되어야 합니다.
- 사용자에게 `USER` 또는 `ADMIN` Realm Role이 부여되어야 합니다.
- Gateway client가 Authorization Code Flow와 redirect URI를 올바르게 설정해야 합니다.
- `/api/**` 요청은 Gateway를 통해 접근하면 `TokenRelay`로 토큰이 전달됩니다.

## 정리

이 샘플은 Spring Cloud Gateway와 OAuth2 Resource Server를 분리해 인증 진입점과 API 보호 경계를 나누는 구조를 보여줍니다. `SecurityConfig`의 핵심은 세 가지입니다.

1. API 경로별 접근 정책을 명확히 선언한다.
2. Keycloak JWT role을 Spring Security authority로 변환한다.
3. issuer뿐 아니라 audience까지 검증해 토큰 사용 대상을 제한한다.

작은 샘플이지만 실무 구조로 확장할 때 중요한 보안 원칙을 담고 있습니다. Gateway는 편의와 라우팅을 담당하고, Resource Server는 자기 API를 스스로 보호해야 합니다.
