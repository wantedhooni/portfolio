# PostgreSQL 16 Streaming Replication + pgpool-II 4.7.1

> Docker Compose 한 줄로 **Primary/Secondary 이중화 + 커넥션 풀 + 읽기 분산**을 로컬에서 재현하는 샘플입니다.

---

## 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [구성 요소](#구성-요소)
3. [디렉터리 구조](#디렉터리-구조)
4. [빠른 시작](#빠른-시작)
5. [포트 정리](#포트-정리)
6. [핵심 설정 해설](#핵심-설정-해설)
   - [Streaming Replication](#streaming-replication)
   - [pgpool-II 커넥션 풀](#pgpool-ii-커넥션-풀)
   - [로드 밸런싱](#로드-밸런싱)
   - [헬스체크 & 페일오버](#헬스체크--페일오버)
   - [인증 (scram-sha-256)](#인증-scram-sha-256)
7. [Secondary 초기화 흐름](#secondary-초기화-흐름)
8. [동작 확인](#동작-확인)
9. [자주 하는 질문](#자주-하는-질문)

---

## 아키텍처 개요

```
                 ┌──────────────────────────────────┐
  Application    │         pgpool-II :5431           │
  (단일 엔드포인트) │  커넥션 풀 · 로드밸런싱 · 페일오버  │
                 └────────┬─────────────┬────────────┘
                          │ WRITE        │ READ (weight 2)
                          ▼             ▼
               ┌──────────────┐  ┌──────────────┐
               │  pg_primary  │  │ pg_secondary │
               │  :5432       │◄─┤  :5433       │
               │  (R/W)       │  │  (Read-only) │
               └──────────────┘  └──────────────┘
                     WAL streaming replication
```

애플리케이션은 **pgpool-II(:5431) 하나**만 바라봅니다.
- `SELECT` 쿼리 → pgpool이 Secondary로 라우팅 (weight 비율 1:2)
- `INSERT / UPDATE / DELETE` → Primary로 라우팅
- Primary 장애 시 → pgpool이 자동 페일오버 처리

---

## 구성 요소

| 컨테이너 | 이미지 | 역할 |
|---|---|---|
| `pg_primary` | `postgres:16` | Primary DB (쓰기 + WAL 송신) |
| `pg_secondary` | `postgres:16` | Secondary DB (읽기 전용 Standby) |
| `pgpool` | `sourcemation/pgpool:4.7.1` | 커넥션 풀 / 로드밸런서 / 페일오버 |

---

## 디렉터리 구조

```
pgpool/
├── docker-compose.yml
├── primary/
│   ├── postgresql.conf          # Primary PostgreSQL 설정
│   ├── pg_hba.conf              # Primary 클라이언트 인증
│   └── init/
│       └── 01_replication_user.sql   # 복제 유저 자동 생성
├── secondary/
│   ├── postgresql.conf          # Secondary PostgreSQL 설정
│   └── init-replica.sh          # pg_basebackup 초기화 스크립트
└── pgpool/
    ├── pgpool.conf              # pgpool-II 메인 설정
    └── pool_hba.conf            # pgpool 클라이언트 인증
```

---

## 빠른 시작

```bash
# 컨테이너 기동 (처음 실행 시 Secondary가 pg_basebackup 수행 — 약 10~20초 소요)
docker compose up -d

# 전체 헬스체크 확인
docker compose ps

# 로그 확인
docker compose logs -f pgpool
docker compose logs -f pg_secondary
```

### 접속 예시

```bash
# pgpool 경유 (애플리케이션 권장)
psql -h localhost -p 5431 -U postgres -d appdb

# Primary 직접 (DBA 작업)
psql -h localhost -p 5432 -U postgres -d appdb

# Secondary 직접 (DBA 작업)
psql -h localhost -p 5433 -U postgres -d appdb
```

비밀번호: `postgres_secret`

---

## 포트 정리

| 포트 | 대상 컨테이너 | 용도 |
|---|---|---|
| `5431` | pgpool | **앱 단일 진입점** — 읽기·쓰기 자동 분산 |
| `5432` | pg_primary | DBA 직접 접속 |
| `5433` | pg_secondary | DBA 직접 접속 |
| `9898` | pgpool | PCP 관리 CLI |

---

## 핵심 설정 해설

### Streaming Replication

**Primary (`primary/postgresql.conf`)**

```ini
wal_level = replica       # 스트리밍 복제 활성화
max_wal_senders = 5       # Secondary 최대 동시 연결
wal_keep_size = 256MB     # Secondary 지연 시 WAL 보존량
wal_log_hints = on        # pg_rewind 지원 (페일오버 후 재동기화)
```

- `wal_level = replica`는 스트리밍 복제의 최소 요구 수준입니다.
- `wal_log_hints = on`은 페일오버 후 구 Primary를 새 Secondary로 재투입할 때 `pg_rewind`가 동작하기 위한 옵션입니다.

**Secondary (`secondary/postgresql.conf`)**

```ini
hot_standby = on           # Standby 상태에서 SELECT 허용
hot_standby_feedback = on  # Primary에 vacuum conflict 전달 (롱쿼리 보호)
```

> `hot_standby_feedback = on`은 Secondary에서 실행 중인 롱쿼리가 Primary VACUUM에 의해 강제 종료되는 현상을 방지합니다. 단, Primary의 dead tuple 회수가 지연될 수 있으므로 트레이드오프를 고려해야 합니다.

---

### pgpool-II 커넥션 풀

```ini
num_init_children = 32   # 최대 동시 클라이언트 수
max_pool = 4             # 프로세스당 캐시 DB 커넥션 수
# 총 DB 커넥션 = 32 × 4 = 128
# PostgreSQL max_connections(200) 보다 작게 유지해야 함
```

pgpool-II는 **프로세스 기반** 커넥션 풀로 동작합니다.

| 파라미터 | 의미 |
|---|---|
| `num_init_children` | fork할 자식 프로세스(= 동시 클라이언트 슬롯) 수 |
| `max_pool` | 각 자식 프로세스가 보유할 수 있는 백엔드 커넥션 캐시 수 |
| `connection_cache = on` | 트랜잭션 종료 후에도 커넥션을 반납하지 않고 재사용 |

**주의:** `num_init_children × max_pool` 값이 PostgreSQL의 `max_connections`를 초과하면 연결 거부가 발생합니다.

---

### 로드 밸런싱

```ini
load_balance_mode = on

backend_hostname0 = 'pg_primary'
backend_weight0 = 1               # 쓰기 전용 — 로드밸런싱 비중 낮음

backend_hostname1 = 'pg_secondary'
backend_weight1 = 2               # 읽기 쿼리 우선 분산
```

- `SELECT` 쿼리는 weight 비율(1:2)에 따라 Primary 또는 Secondary로 분산됩니다.
- `INSERT / UPDATE / DELETE` 및 트랜잭션 내부 쿼리는 항상 Primary로 전달됩니다.
- `backend_flag0 = 'ALWAYS_PRIMARY'`를 지정해 pgpool이 Primary 노드를 명시적으로 인식하도록 합니다.

---

### 헬스체크 & 페일오버

```ini
health_check_period = 10       # 10초마다 백엔드 헬스 확인
health_check_timeout = 5
health_check_max_retries = 3
health_check_retry_delay = 1

sr_check_period = 10           # Streaming Replication 상태 주기적 확인
sr_check_user = 'replicator'

failover_on_backend_error = on # 백엔드 오류 시 자동 페일오버
```

pgpool은 두 가지 방식으로 백엔드를 모니터링합니다:

1. **Health Check**: `pg_isready`와 유사하게 단순 접속 가능 여부 확인
2. **SR Check**: Streaming Replication 지연(lag) 및 복제 상태 확인

Primary 장애가 감지되면 `failover_on_backend_error = on`에 의해 해당 노드를 자동으로 분리합니다.

---

### 인증 (scram-sha-256)

`pool_hba.conf`와 `pg_hba.conf`의 인증 방식은 **반드시 일치**해야 합니다.

```
# pool_hba.conf (pgpool 클라이언트 → pgpool)
host  all  all  0.0.0.0/0  scram-sha-256

# pg_hba.conf (pgpool → PostgreSQL 백엔드)
host  all  all  0.0.0.0/0  scram-sha-256
```

방식이 다를 경우 pgpool이 클라이언트로부터 받은 인증 토큰을 백엔드에 전달하지 못해 인증 오류가 발생합니다.

---

## Secondary 초기화 흐름

`init-replica.sh`가 Secondary 컨테이너 기동 시 자동으로 실행됩니다.

```
[secondary 컨테이너 기동]
        │
        ▼
standby.signal 파일 존재? ──YES──► PostgreSQL 직접 기동 (재기동 케이스)
        │ NO
        ▼
Primary 접속 대기 (pg_isready 루프)
        │
        ▼
PGDATA 디렉터리 초기화
        │
        ▼
pg_basebackup -Fp -Xs -R
  (-R: primary_conninfo + standby.signal 자동 생성)
        │
        ▼
PostgreSQL 기동 (Standby 모드)
```

`-R` 플래그 덕분에 `postgresql.auto.conf`에 `primary_conninfo`가 자동 기록되고, `standby.signal` 파일이 생성되어 PostgreSQL이 Standby 모드로 기동됩니다.

---

## 동작 확인

### 복제 상태 확인

```sql
-- Primary에서 실행
SELECT client_addr, state, sent_lsn, write_lsn, replay_lsn, sync_state
FROM pg_stat_replication;
```

### 로드밸런싱 확인

```sql
-- pgpool 경유로 여러 번 실행하면 Primary/Secondary가 번갈아 응답
SELECT inet_server_addr(), inet_server_port();
```

### pgpool 노드 상태 확인

```bash
# PCP CLI로 노드 상태 조회
docker exec pgpool pcp_node_info -h 127.0.0.1 -p 9898 -u pgpool_admin -w -n 0
docker exec pgpool pcp_node_info -h 127.0.0.1 -p 9898 -u pgpool_admin -w -n 1
```

### 컨테이너 정지 및 재시작

```bash
# 전체 종료 (볼륨 유지)
docker compose down

# 전체 초기화 (볼륨 삭제 — Secondary도 재복제)
docker compose down -v
```

---

## 자주 하는 질문

**Q. Secondary 기동이 늦어져 pgpool이 오류를 내는데요?**

`docker-compose.yml`의 `depends_on` 조건이 `service_healthy`로 설정되어 있어, Secondary의 헬스체크가 통과한 뒤에 pgpool이 기동됩니다. Primary 준비 → Secondary 복제 완료 → pgpool 기동 순으로 진행되므로, 첫 실행 시 20~30초 정도 소요될 수 있습니다.

**Q. `max_connections`는 왜 200으로 맞춰야 하나요?**

Streaming Replication에서 Secondary의 `max_connections`, `max_worker_processes` 등 일부 파라미터는 Primary보다 같거나 커야 합니다. 이를 어기면 Secondary 기동 시 `FATAL: recovery aborted because of insufficient parameter settings` 오류가 발생합니다.

**Q. `hot_standby_feedback = on`의 부작용은?**

Secondary에서 롱쿼리가 실행 중일 때 Primary의 VACUUM이 해당 row version을 회수하지 못하고 대기합니다. 테이블 bloat이 심해질 수 있으므로, 롱쿼리가 빈번한 환경에서는 `vacuum_defer_cleanup_age` 등과 함께 튜닝이 필요합니다.

**Q. Primary 장애 후 수동 복구는?**

현재 설정은 `failover_on_backend_error = on`으로 자동 분리만 수행합니다. Secondary의 Primary 승격이나 구 Primary의 재투입은 `pg_rewind` + PCP 커맨드를 사용한 수동 절차가 필요합니다. 완전 자동 페일오버가 필요하다면 `recovery_1st_stage_command` 스크립트를 추가로 구성하세요.