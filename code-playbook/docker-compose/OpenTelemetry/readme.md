# Monitoring Stack

통합 Observability 환경을 위한 Docker Compose 기반 Monitoring Stack입니다.

## Architecture

| Category              | Component               | Tool                        |
| --------------------- | ----------------------- | --------------------------- |
| Telemetry Pipeline    | OpenTelemetry Collector | OTLP 수집 및 Export            |
| Metrics               | Prometheus              | 메트릭 수집 및 저장                 |
| Logs                  | Loki                    | 로그 저장소                      |
| Tracing               | Jaeger                  | Distributed Tracing UI      |
| Tracing               | Tempo                   | Trace Backend               |
| Dashboard             | Grafana                 | 통합 대시보드 및 시각화               |
| Traffic Visualization | Kiali                   | Service Mesh 시각화 (Istio 필요) |
| Alerting              | AlertManager            | 알림 관리                       |

---

# Start

```bash
docker compose up -d
```

실행 상태 확인

```bash
docker compose ps
```

로그 확인

```bash
docker compose logs -f
```

---

# Dashboard URLs
## Dashboard URLs

| Component                         | Local URL                     | Username     | Password     | Notes                             |
| --------------------------------- | ----------------------------- | ------------ | ------------ | --------------------------------- |
| Grafana                           | http://localhost:3000         | `admin`      | `admin`      | 통합 Dashboard 및 Visualization      |
| Prometheus                        | http://localhost:9090         | -            | -            | Metrics 조회 및 Target 상태            |
| AlertManager                      | http://localhost:9093         | -            | -            | Alert 관리                          |
| Jaeger UI                         | http://localhost:16686        | -            | -            | Distributed Trace 조회              |
| Tempo                             | http://localhost:3200         | -            | -            | 별도 UI 없음 (Grafana Datasource로 사용) |
| Loki                              | http://localhost:3100         | -            | -            | 별도 UI 없음 (Grafana Datasource로 사용) |
| Kiali                             | http://localhost:20001/kiali  | 환경 설정에 따라 다름 | 환경 설정에 따라 다름 | Istio 기반 Service Mesh 시각화         |
| OpenTelemetry Collector Metrics   | http://localhost:8888/metrics | -            | -            | Collector 내부 메트릭 엔드포인트            |
| OpenTelemetry Prometheus Exporter | http://localhost:8889/metrics | -            | -            | Prometheus Scrape 대상 메트릭          |

> **참고**
>
> * `Grafana`에서 `Prometheus`, `Loki`, `Tempo`를 Datasource로 등록하면 Metrics, Logs, Traces를 하나의 UI에서 조회할 수 있습니다.
> * `Loki`와 `Tempo`는 기본적으로 별도의 Dashboard UI를 제공하지 않으며 API Endpoint만 노출합니다.
> * `Jaeger UI`는 Trace 탐색 전용 웹 인터페이스를 제공합니다.
> * `Kiali`는 Kubernetes + Istio 환경을 전제로 설계되었으며, 단순 Docker Compose 환경에서는 일부 기능이 제한될 수 있습니다.

---
# Default Credentials

## Grafana

| Item     | Value   |
| -------- | ------- |
| Username | `admin` |
| Password | `admin` |

---

# Internal Service Endpoints

Docker Compose 네트워크 내부에서 사용하는 주소입니다.

| Service                 | Endpoint                            |
| ----------------------- | ----------------------------------- |
| Prometheus              | `http://prometheus:9090`            |
| Loki                    | `http://loki:3100`                  |
| Jaeger                  | `http://jaeger:16686`               |
| Tempo                   | `http://tempo:3200`                 |
| AlertManager            | `http://alertmanager:9093`          |
| OpenTelemetry Collector | `http://otel-collector:4317` (gRPC) |
| OpenTelemetry Collector | `http://otel-collector:4318` (HTTP) |

---

# Data Flow

```text
                 ┌─────────────────────┐
                 │  Application / SDK  │
                 └──────────┬──────────┘
                            │ OTLP
                            ▼
              ┌─────────────────────────────┐
              │ OpenTelemetry Collector     │
              └───────┬─────────┬───────────┘
                      │         │
         Metrics      │         │ Logs
                      │         │
                      ▼         ▼
             ┌────────────┐ ┌──────────┐
             │Prometheus  │ │   Loki   │
             └────────────┘ └──────────┘
                      │
                      │
                      ▼
                 ┌──────────┐
                 │ Grafana  │
                 └──────────┘
                      ▲
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
   ┌──────────┐               ┌──────────┐
   │ Jaeger   │               │  Tempo   │
   └──────────┘               └──────────┘
```

---

# Port Summary

| Port    | Service                         |
| ------- | ------------------------------- |
| `3000`  | Grafana                         |
| `3100`  | Loki                            |
| `3200`  | Tempo                           |
| `4317`  | OpenTelemetry OTLP gRPC         |
| `4318`  | OpenTelemetry OTLP HTTP         |
| `8888`  | OpenTelemetry Collector Metrics |
| `8889`  | Prometheus Exporter             |
| `9090`  | Prometheus                      |
| `9093`  | AlertManager                    |
| `16686` | Jaeger UI                       |
| `20001` | Kiali                           |

---

# Notes

* **Grafana**는 Prometheus, Loki, Jaeger, Tempo를 하나의 UI에서 통합 조회할 수 있습니다.
* **Tempo**와 **Loki**는 별도의 Web UI를 제공하지 않으며, 일반적으로 Grafana를 통해 조회합니다.
* **Jaeger**는 Trace 탐색 전용 UI를 제공합니다.
* **Kiali**는 Kubernetes와 Istio Service Mesh 환경을 전제로 설계되었습니다. 단순 Docker Compose 환경에서는 기능이 제한되거나 정상적으로 동작하지 않을 수 있습니다.
* OpenTelemetry Collector는 Metrics, Logs, Traces를 수집하여 각 백엔드로 전달하는 중앙 파이프라인 역할을 수행합니다.
