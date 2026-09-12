# Spring Cloud MSA Skeleton

## 구성(포트)
- mariadb (3306)
- config-server (8888): Spring Cloud Config Server (native, classpath:/config)
- discovery-server (8761): Eureka Server
- api-gateway (8080): Spring Cloud Gateway(WebFlux) + JWT Resource Server + OpenAPI Aggregation
- auth-service (9000): JWT 발급(HS256) + MariaDB/JPA (authdb)
- user-service (9001): User API + MariaDB/JPA (userdb)
- order-service (9002): Order API + MariaDB/JPA (orderdb)

## 원샷 기동(Docker)
```bash
docker compose up --build
```

## Swagger UI
- Gateway Aggregation: http://localhost:8080/swagger-ui.html
  - auth-service: /auth/v3/api-docs
  - user-service: /user/v3/api-docs
  - order-service: /order/v3/api-docs

## 토큰 발급/호출(로컬)
1) 토큰 발급
```bash
curl -s -X POST http://localhost:9000/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}'
```

2) Gateway 통해 user-service 호출
```bash
TOKEN="(위 결과의 accessToken)"
curl -s http://localhost:8080/user/api/v1/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

## 데모 계정
- user / pass
- admin / pass

## 사용시 필요, 고려 작업
- api-gateway SecurityConfig 설정
- - SecurityFilterChain 상세 설정필요 (구축 / 셋팅을 위해 작성했다 제거했음)