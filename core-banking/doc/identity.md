# 사용자·관리자 (User & Admin / RBAC)

> 서비스 회원(User)과 운영 관리자(Admin)의 인증 주체를 관리하고,
> 관리자에 대해 **역할 기반 접근 제어(RBAC)** 를 제공하는 도메인.

---

## 1. 용어 정리

| 용어 | 영문/필드 | 설명 |
|------|-----------|------|
| 회원 | User | SaaS(고객) 애플리케이션 로그인 주체 |
| 관리자 | Admin | 운영(Admin) 애플리케이션 로그인 주체 |
| 역할 | AdminRole | 권한들의 묶음(예: 운영자, 회계담당) |
| 권한 | AdminPermission | 개별 행위 단위 권한(USER_READ 등) |
| RBAC | Role-Based Access Control | 역할에 권한을 부여하고, 관리자에 역할을 부여 |

---

## 2. 회원(User)

- 필드: `email`(유니크), `password`(암호화 저장), `name`, `role`(기본 "USER").
- 생성: `User.create(email, password, name)` — 기본 역할 USER.
- 행위: `updateName`, `updatePassword`(인코딩된 값), `changeRole`.
- 인증은 SaaS 애플리케이션(`api-saas`)의 JWT로 처리(계약자/피보험자/계좌주 등 모든 고객 행위의 주체).

---

## 3. 관리자(Admin)와 RBAC

### 구조
```
Admin ──(N:M)── AdminRole ──(1:N permissions)── AdminPermission(enum)
```
- `Admin`은 여러 `AdminRole`을 가질 수 있음(`assignRole`/`removeRole`).
- `AdminRole`은 권한 집합(`Set<AdminPermission>`)을 `EnumSet`으로 보유.
- 권한은 `@ElementCollection`(EAGER)으로 역할과 함께 로딩.

### 권한 카탈로그(AdminPermission)
도메인별 읽기/쓰기 + 특수 행위로 구성:

| 영역 | 권한 |
|------|------|
| 사용자 | USER_READ, USER_WRITE |
| 계좌 | ACCOUNT_READ, ACCOUNT_WRITE, ACCOUNT_TRANSFER |
| 종목 | STOCK_READ, STOCK_WRITE |
| 주문 | ORDER_READ, ORDER_WRITE, ORDER_EXECUTE, ORDER_CANCEL |
| 체결 | TRADE_READ |
| 외환 | FX_READ, FX_WRITE, FX_CONVERT |
| 보험 | INSURANCE_READ, INSURANCE_WRITE, INSURANCE_CLAIM |
| 원장 | LEDGER_READ, LEDGER_WRITE |
| 관리자 | ADMIN_READ, ADMIN_WRITE |
| 역할 | ROLE_READ, ROLE_WRITE |
| 스케줄러 | SCHEDULER_READ, SCHEDULER_WRITE |

> **읽기/쓰기 분리 + 특수권한**(TRANSFER, EXECUTE, CONVERT, CLAIM)으로 최소권한 원칙을 적용.
> 예: 조회만 가능한 운영자에게 ACCOUNT_READ만, 이체 승인자에게만 ACCOUNT_TRANSFER 부여.

### 역할 관리 플로우
```
1. 역할 생성   AdminRole.create(name, description, permissions)
2. 권한 변경   role.update(name, description, permissions)
3. 관리자 부여 admin.assignRole(role) / removeRole(role)
```

---

## 4. 인증 분리 — 두 개의 애플리케이션

| 주체 | 애플리케이션 | 토큰 주체 클래스 |
|------|-------------|------------------|
| 회원 User | `api-saas` | UserJwtPrincipal |
| 관리자 Admin | `api-admin` | AdminJwtPrincipal |

- 고객용/운영용 JWT 발급·검증을 **물리적으로 분리**하여 권한 경계를 명확히 함.
- 관리자 API는 컨트롤러/메서드 단위로 `AdminPermission`을 검사(RBAC 적용 지점).

---

## 5. 현업 기준 유의사항

- **비밀번호는 항상 인코딩 저장**(평문 금지). 변경 시에도 인코딩된 값만 주입.
- **최소권한 원칙(PoLP)**: 역할에 꼭 필요한 권한만. 읽기/쓰기/특수행위 분리로 세밀하게 통제.
- **권한 경계**: 회원(User)과 관리자(Admin)는 서로 다른 인증 도메인 — 토큰 혼용 불가.
- **감사**: 민감 행위(이체·주문체결·보험금지급·기간마감)는 행위 관리자 ID를 기록(원장·청구·정산 도메인 참조).
