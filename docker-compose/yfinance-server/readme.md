# yfinance-server 서버

yahoo finance 주식 조회 서버
https://query1.finance.yahoo.com/ 로 직접 API만들어서 호출하니 보안에 걸려서 우회용 서버를 사용한다.

## Why Use This?
- **Language-agnostic HTTP API**: Expose Yahoo Finance data to any platform without embedding Python.
- **Containerized deployment**: Ready Docker image and compose stack for Prometheus + Grafana.
- **Extendable FastAPI app**: Easy to add routes, middleware, or auth.
- **Caching & instrumentation**: Includes a TTL in-memory cache with async locks and Prometheus metrics.
- **Robust yfinance wrapper**: Calls are wrapped with timeouts, `lru_cache` ticker caching, and async-to-thread execution to reduce upstream variability.
- **Observability**: `/metrics` endpoint and sample Grafana dashboards for latency, cache, and error monitoring.

## Features
| Feature                | Description                                             |
| ---------------------- | ------------------------------------------------------- |
| **Quote API**          | Fetch latest market quotes (OHLCV) for ticker symbols.  |
| **Historical API**     | Retrieve historical data with flexible intervals (1h, 1d, 1wk, 1mo). |
| **Info API**           | Get company fundamentals (sector, market cap, etc.).    |
| **Earnings API**       | Retrieve normalized earnings history with EPS, revenue, and surprise data.    |
| **Snapshot API**       | Combined info + quote in a single request with caching. |
| **Health Check**       | `/health` and `/ready` endpoints for liveness & readiness probes. |
| **Prometheus Metrics** | `/metrics` endpoint for request count, errors, latency, cache stats. |

## API Endpoints

| Endpoint                                                   | Description               | Example                                            |
| ---------------------------------------------------------- | ------------------------- | -------------------------------------------------- |
| `GET /quote/{symbol}`                                      | Latest quote for a symbol | `/quote/AAPL`                                      |
| `GET /quote?symbols=SYM1,SYM2`                             | Bulk quotes (CSV)         | `/quote?symbols=AAPL,MSFT`                         |
| `GET /historical/{symbol}?start=&end=&interval=`           | Historical OHLCV data     | `/historical/AAPL?start=2024-01-01&end=2024-02-01&interval=1d` |
| `GET /info/{symbol}`                                       | Company details           | `/info/TSLA`                                       |
| `GET /health`                                              | Health check              | `/health`                                          |
| `GET /metrics`                                             | Prometheus metrics        | `/metrics`                                         |
| `GET /earnings/{symbol}?frequency={period}`        | Earnings history (EPS, revenue, surprise) | `/earnings/AAPL?frequency=quarterly` |
| `GET /snapshot/{symbol}`                                   | Combined info + quote     | `/snapshot/AAPL`                                   |
| `GET /ready`                                               | Readiness check (yfinance connectivity) | `/ready`                             |

Then visit:
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

Access:
- API: http://localhost:8000
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

출처: https://github.com/Vorckea/yfinance-service 
