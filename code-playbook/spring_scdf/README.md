# spring_scdf

Spring Cloud Data Flow를 로컬에서 띄워보는 인프라 중심 샘플입니다.

## 현재 구현

- `docker-compose.yaml` 기반 로컬 구성
- PostgreSQL
- RabbitMQ
- Skipper
- Spring Cloud Data Flow Server

## 접속 정보

| 항목 | 주소 |
| --- | --- |
| SCDF UI | http://localhost:9393/dashboard/index.html#/apps |
| Skipper API | http://localhost:7577/api |
| RabbitMQ UI | http://localhost:15672 |
| PostgreSQL | localhost:5432 |

## 기본 계정

- PostgreSQL
  - user: `scdf`
  - password: `scdf`
- RabbitMQ
  - user: `guest`
  - password: `guest`

## 현재 상태

- 애플리케이션 코드보다는 SCDF 인프라를 직접 띄워보는 목적의 모듈입니다.
- `src` 아래 구현 코드는 아직 없습니다.
