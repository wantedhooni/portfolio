# spring_security_jwt

Spring Boot 기반 JWT 인증/인가 연습 프로젝트입니다.

## 구현 범위

- 이메일 회원가입
- 로그인 후 `accessToken`, `refreshToken` 발급
- Redis 기반 refresh token 저장
- access token 블랙리스트 로그아웃 처리
- refresh token 재발급
- 로그인 사용자 정보 조회
- 회원 탈퇴
- JPA + Querydsl 기반 사용자/권한 도메인

## 주요 API

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/auth/reissue`
- `POST /api/user/logout`
- `GET /api/user/me`
- `DELETE /api/user/withdraw`

## 실행 메모

- 애플리케이션: `./gradlew bootRun`
- Redis: `docker compose up -d`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Console: `http://localhost:8080/h2-console`
- H2 JDBC: `jdbc:h2:tcp://localhost:13306/mem:testdb`
- Redis UI: `http://localhost:8081/`

## 기술 포인트

- `JwtAuthenticationFilter`로 Bearer 토큰 인증
- `RedisTokenStore`로 refresh token, 블랙리스트 토큰 관리
- `DataInitializer`로 기본 권한/샘플 데이터 초기화
- DTO를 controller payload와 service dto로 분리

## 참고

- 현재 JWT secret은 `application.yml`에 예제로 들어가 있으므로 실사용 시 외부 설정으로 분리해야 합니다.
