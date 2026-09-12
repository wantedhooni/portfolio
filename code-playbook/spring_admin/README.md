# spring_admin

Spring Boot Admin 서버와 클라이언트를 멀티 모듈로 구성한 샘플입니다.

## 모듈 구성

- `admin_server`: Spring Boot Admin Server
- `app_client`: Admin에 등록되는 샘플 애플리케이션

## 접속 정보

- Admin Server: `http://localhost:18080`
- Client App: `http://localhost:8081`
- 기본 계정: `admin / admin1234`

## 구현 범위

- `@EnableAdminServer` 기반 Admin 서버
- Spring Security 로그인 보호
- Client의 Actuator 정보 노출
- Client가 Server에 자동 등록

## 실행 예시

- 서버: `./gradlew :admin_server:bootRun`
- 클라이언트: `./gradlew :app_client:bootRun`

## 메모

- `SecurityConfig`에서 로그인 페이지, Basic Auth, CSRF 예외 경로를 직접 설정합니다.
- 실무에서는 인증 정보와 접속 URL을 외부 설정으로 분리하는 편이 안전합니다.
