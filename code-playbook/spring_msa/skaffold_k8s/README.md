# skaffold_k8s

Spring Boot 기반 MSA 예제를 Docker, Kubernetes raw YAML, Skaffold로 빌드하고 배포하는 샘플 프로젝트입니다.

이 프로젝트는 Helm을 사용하지 않고 `k8s/**/*.yaml` 파일을 직접 작성한 뒤, Skaffold가 해당 YAML을 렌더링/배포하는 구조입니다.

핵심 목표는 다음입니다.

1. Gradle 멀티 모듈로 여러 Spring Boot 서비스를 관리한다.
2. 각 서비스를 Docker 이미지로 빌드한다.
3. Kubernetes `ConfigMap`, `Deployment`, `Service` YAML을 직접 작성한다.
4. Skaffold로 이미지 빌드, Kubernetes manifest 적용, port-forward를 자동화한다.

---

## 전체 구조

```text
skaffold_k8s
├── application
│   ├── app-1
│   │   ├── Dockerfile
│   │   ├── build.gradle
│   │   └── src/main
│   └── app-2
│       ├── Dockerfile
│       ├── build.gradle
│       └── src/main
├── core
│   └── common-webmvc
│       └── build.gradle
├── shared-server
│   ├── api-gateway
│   │   ├── Dockerfile
│   │   ├── build.gradle
│   │   └── src/main
│   └── discovery-server
│       ├── Dockerfile
│       ├── build.gradle
│       └── src/main
├── k8s
│   ├── api-gateway
│   │   ├── config.yaml
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   ├── app-1
│   │   ├── config.yaml
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   ├── app-2
│   │   ├── config.yaml
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── discovery-server
│       ├── config.yaml
│       ├── deployment.yaml
│       └── service.yaml
├── docker-compose-Infra.yml
├── docker-compose-app.yml
├── skaffold.yaml
├── settings.gradle
├── build.gradle
└── gradle.properties
```

---

## 모듈 구성

### `core:common-webmvc`

공통 WebMVC 의존성을 관리하는 라이브러리 모듈입니다.

주요 의존성:

- Spring Boot Actuator
- SpringDoc OpenAPI WebMVC UI
- Eureka Client

`app-1`, `app-2`가 이 모듈을 의존합니다.

### `shared-server:discovery-server`

Eureka Server 역할을 하는 서비스 디스커버리 서버입니다.

- 기본 포트: `8761`
- Kubernetes Service 이름: `shared-server-discovery-server`
- 각 서비스가 자신을 등록하는 서비스 레지스트리 역할

### `shared-server:api-gateway`

외부 요청을 내부 서비스로 라우팅하는 API Gateway입니다.

- 기본 포트: `8080`
- Kubernetes Service 이름: `shared-server-api-gateway`
- Spring Cloud Gateway WebFlux 사용
- Eureka를 통해 `app-1`, `app-2` 위치 조회

현재 라우팅:

| 외부 요청 경로 | 대상 Eureka 서비스 |
| --- | --- |
| `/api/app-1/**` | `APP-1` |
| `/api/app-2/**` | `APP-2` |

### `application:app-1`

샘플 비즈니스 서비스 1번입니다.

- 기본 포트: `10000`
- Kubernetes Service 이름: `application-app-1`
- 테스트 API: `GET /api/app-1/test`
- 응답 예시: `app_1 Hello World`

### `application:app-2`

샘플 비즈니스 서비스 2번입니다.

- 기본 포트: `11000`
- Kubernetes Service 이름: `application-app-2`
- 테스트 API: `GET /api/app-2/test`
- 응답 예시: `app_2 Hello World`

---

## 서비스 포트

| 서비스 | Kubernetes Service | 포트 | 설명 |
| --- | --- | ---: | --- |
| `api-gateway` | `shared-server-api-gateway` | `8080` | 외부 진입점 |
| `discovery-server` | `shared-server-discovery-server` | `8761` | Eureka Server |
| `app-1` | `application-app-1` | `10000` | 샘플 애플리케이션 1 |
| `app-2` | `application-app-2` | `11000` | 샘플 애플리케이션 2 |
| `postgres` | - | `5432` | 로컬 PostgreSQL |
| `otel-lgtm` | - | `3000` | Grafana UI |
| `otel-lgtm` | - | `4317` | OTLP gRPC |
| `otel-lgtm` | - | `4318` | OTLP HTTP |

---

## 기술 스택

| 영역 | 사용 기술 |
| --- | --- |
| Language | Java 21 |
| Build | Gradle |
| Framework | Spring Boot 4.1.0 |
| Cloud | Spring Cloud 2025.1.2 |
| Service Discovery | Netflix Eureka |
| Gateway | Spring Cloud Gateway WebFlux |
| Container | Docker |
| Local Infra | Docker Compose |
| Kubernetes 배포 | raw Kubernetes YAML |
| 개발 배포 자동화 | Skaffold |
| Observability 샘플 | Grafana OTEL LGTM |
| Database 샘플 | PostgreSQL 17.4 |

---

## 요청 흐름

```text
Client
  ↓
API Gateway :8080
  ↓
Eureka에서 APP-1 또는 APP-2 위치 조회
  ↓
app-1 :10000 또는 app-2 :11000
```

Gateway 호출 예시:

```bash
curl http://localhost:8080/api/app-1/test
curl http://localhost:8080/api/app-2/test
```

---

## Gradle 빌드

전체 프로젝트 빌드:

```bash
./gradlew clean build
```

테스트 제외 빌드:

```bash
./gradlew clean build -x test
```

개별 서비스 `bootJar` 생성:

```bash
./gradlew :shared-server:discovery-server:bootJar
./gradlew :shared-server:api-gateway:bootJar
./gradlew :application:app-1:bootJar
./gradlew :application:app-2:bootJar
```

---

## Docker Compose 실행

### 인프라 실행

인프라 구성:

- PostgreSQL
- Grafana OTEL LGTM

```bash
docker compose -f docker-compose-Infra.yml up -d
```

종료:

```bash
docker compose -f docker-compose-Infra.yml down -v
```

### 애플리케이션 실행

```bash
docker compose -f docker-compose-app.yml up --build
```

종료:

```bash
docker compose -f docker-compose-app.yml down
```

### 스크립트 주의사항

현재 `run_all.sh`, `run_infra.sh`는 `docker-compose-infra.yml` 파일명을 참조합니다.

실제 파일명은 다음과 같습니다.

```text
docker-compose-Infra.yml
```

대소문자를 구분하는 파일시스템에서는 스크립트가 실패할 수 있으므로 파일명 또는 스크립트 참조명을 맞춰야 합니다.

---

## Docker 이미지 빌드 방식

각 서비스는 개별 `Dockerfile`을 가지고 있습니다.

| 서비스 | Dockerfile | Skaffold 이미지명 |
| --- | --- | --- |
| `api-gateway` | `shared-server/api-gateway/Dockerfile` | `shared-server-api-gateway` |
| `discovery-server` | `shared-server/discovery-server/Dockerfile` | `shared-server-discovery-server` |
| `app-1` | `application/app-1/Dockerfile` | `application-app-1` |
| `app-2` | `application/app-2/Dockerfile` | `application-app-2` |

Dockerfile은 공통적으로 다음 방식입니다.

1. `eclipse-temurin:21-jdk` 이미지에서 Gradle `bootJar`를 실행한다.
2. 생성된 JAR 파일을 `eclipse-temurin:21-jre` 런타임 이미지로 복사한다.
3. `java -jar app.jar`로 서비스를 실행한다.

---

## Kubernetes 구성

이 프로젝트는 Helm Chart를 사용하지 않고 서비스별 raw YAML을 직접 관리합니다.

각 서비스는 기본적으로 3개 파일로 구성됩니다.

```text
k8s/{서비스명}
├── config.yaml
├── deployment.yaml
└── service.yaml
```

파일 역할:

| 파일 | 역할 |
| --- | --- |
| `config.yaml` | 서비스 실행에 필요한 환경변수 정의 |
| `deployment.yaml` | Pod 생성 방식, replica 수, 컨테이너 이미지 정의 |
| `service.yaml` | Kubernetes 내부에서 접근 가능한 고정 네트워크 이름과 포트 제공 |

현재 manifest 경로:

```text
k8s/api-gateway/*.yaml
k8s/app-1/*.yaml
k8s/app-2/*.yaml
k8s/discovery-server/*.yaml
```

---

## Skaffold 구성

`skaffold.yaml`은 다음 작업을 담당합니다.

1. 서비스별 Docker 이미지 빌드
2. `k8s/**/*.yaml` raw manifest 적용
3. 로컬 개발용 port-forward 연결

현재 이미지 빌드 대상:

| 이미지 | Dockerfile |
| --- | --- |
| `application-app-1` | `application/app-1/Dockerfile` |
| `application-app-2` | `application/app-2/Dockerfile` |
| `shared-server-api-gateway` | `shared-server/api-gateway/Dockerfile` |
| `shared-server-discovery-server` | `shared-server/discovery-server/Dockerfile` |

현재 raw manifest 대상:

```yaml
manifests:
  rawYaml:
    - k8s/api-gateway/*.yaml
    - k8s/app-1/*.yaml
    - k8s/app-2/*.yaml
    - k8s/discovery-server/*.yaml
```

현재 port-forward 대상:

| Kubernetes Service | 로컬 포트 |
| --- | ---: |
| `application-app-1` | `10000` |
| `application-app-2` | `11000` |
| `shared-server-api-gateway` | `8080` |
| `shared-server-discovery-server` | `8761` |

---

## Skaffold 실행

개발 모드:

```bash
skaffold dev
```

1회 빌드 및 배포:

```bash
skaffold run
```

렌더링 결과만 확인:

```bash
skaffold render
```

삭제:

```bash
skaffold delete
```

배포 상태 확인:

```bash
kubectl get pods
kubectl get svc
```

로그 확인:

```bash
kubectl logs -l app=shared-server-api-gateway -f
kubectl logs -l app=application-app-1 -f
```

---

## 신규 서비스 추가 작성 순서

예시는 `app-3` 서비스를 추가한다고 가정합니다.

실무에서는 아래 순서로 작성하면 누락을 줄일 수 있습니다.

1. `application/app-3` Spring Boot 모듈을 생성한다.
2. `settings.gradle`에 `include(":application:app-3")`를 추가한다.
3. `application/app-3/build.gradle`을 작성한다.
4. `application/app-3/src/main/resources/application.yml`에 서비스명과 포트를 설정한다.
5. `application/app-3/Dockerfile`을 작성한다.
6. `k8s/app-3/config.yaml`을 작성한다.
7. `k8s/app-3/deployment.yaml`을 작성한다.
8. `k8s/app-3/service.yaml`을 작성한다.
9. `shared-server/api-gateway/src/main/resources/application.yml`에 Gateway route를 추가한다.
10. `skaffold.yaml`의 `build.artifacts`에 이미지 빌드 설정을 추가한다.
11. `skaffold.yaml`의 `manifests.rawYaml`에 `k8s/app-3/*.yaml`을 추가한다.
12. 필요하면 `skaffold.yaml`의 `portForward`에 `app-3` 포트를 추가한다.
13. `skaffold render`로 manifest 결과를 확인한다.
14. `skaffold dev`로 개발 배포를 실행한다.

---

## 이름 규칙 권장안

이 프로젝트는 Gradle 모듈명, Docker 이미지명, Kubernetes 리소스명을 분리해서 사용합니다.

| 항목 | 권장 값 예시 | 설명 |
| --- | --- | --- |
| Gradle module | `:application:app-3` | Gradle 빌드 대상 |
| Spring app name | `app-3` | Eureka 등록 이름 |
| Docker image | `application-app-3` | Skaffold 빌드 이미지 |
| Deployment | `application-app-3` | Kubernetes Deployment 이름 |
| Pod label | `app: application-app-3` | Deployment/Service 연결 기준 |
| Service | `application-app-3` | Kubernetes 내부 DNS 이름 |
| ConfigMap | `app-3-config` | 환경변수 저장 |
| container name | `application-app-3` | Pod 내부 컨테이너 이름 |
| service port | `12000` | Kubernetes Service port |
| container port | `12000` | Spring Boot `server.port` |

Gateway에서 Eureka 기반 라우팅을 추가할 때는 Spring application name이 대문자로 조회되는 점을 고려합니다.

```yaml
uri: lb://APP-3
```

---

## Spring 설정 템플릿

`application/app-3/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: app-3
  profiles:
    default: local

server:
  port: 12000

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessstate:
      enabled: true
    readinessstate:
      enabled: true

eureka:
  client:
    service-url:
      defaultZone: ${DISCOVERY_SERVER_URL:http://localhost:8761/eureka/}
```

---

## Dockerfile 템플릿

`application/app-3/Dockerfile`

```dockerfile
ARG GRADLE_PROJECT=:application:app-3
ARG MODULE_DIR=application/app-3

FROM eclipse-temurin:21-jdk AS build
ARG GRADLE_PROJECT

WORKDIR /workspace
COPY . .
RUN ./gradlew ${GRADLE_PROJECT}:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
ARG MODULE_DIR

WORKDIR /app
COPY --from=build /workspace/${MODULE_DIR}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

shared server 모듈이면 아래 값만 바꿉니다.

```dockerfile
ARG GRADLE_PROJECT=:shared-server:some-server
ARG MODULE_DIR=shared-server/some-server
```

---

## Kubernetes 서비스 템플릿

새 서비스는 아래 3개 파일을 복사해서 서비스명, 이미지명, 포트만 바꿔 사용하면 됩니다.

디렉터리 생성:

```bash
mkdir -p k8s/app-3
```

---

## `config.yaml` 템플릿

`k8s/app-3/config.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-3-config
data:
  DISCOVERY_SERVER_URL: http://shared-server-discovery-server:8761/eureka/
```

작성 기준:

- `metadata.name`은 Deployment의 `configMapRef.name`과 같아야 한다.
- `DISCOVERY_SERVER_URL`은 Kubernetes Service 이름을 사용한다.
- 현재 Discovery Server Service 이름은 `shared-server-discovery-server`다.

---

## `deployment.yaml` 템플릿

`k8s/app-3/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: application-app-3
  labels:
    app: application-app-3
spec:
  replicas: 1
  selector:
    matchLabels:
      app: application-app-3
  template:
    metadata:
      labels:
        app: application-app-3
    spec:
      containers:
        - name: application-app-3
          image: application-app-3
          imagePullPolicy: IfNotPresent
          envFrom:
            - configMapRef:
                name: app-3-config
          ports:
            - name: http
              containerPort: 12000
```

작성 기준:

- `metadata.name`은 Deployment 이름이다.
- `metadata.labels.app`, `spec.selector.matchLabels.app`, `template.metadata.labels.app`, Service `selector.app`은 반드시 같아야 한다.
- `image`는 `skaffold.yaml`의 `build.artifacts[].image`와 같아야 한다.
- `containerPort`는 Spring Boot `server.port`와 같아야 한다.
- 로컬 Skaffold 빌드 이미지를 사용할 때는 `imagePullPolicy: IfNotPresent`가 안전하다.

운영에 가까운 probe 추가 예시:

```yaml
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 20
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 30
            periodSeconds: 10
```

리소스 제한 예시:

```yaml
          resources:
            requests:
              cpu: 100m
              memory: 256Mi
            limits:
              cpu: 500m
              memory: 512Mi
```

---

## `service.yaml` 템플릿

`k8s/app-3/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: application-app-3
  labels:
    app: application-app-3
spec:
  type: ClusterIP
  ports:
    - name: http
      port: 12000
      targetPort: 12000
      protocol: TCP
  selector:
    app: application-app-3
```

작성 기준:

- `metadata.name`은 Kubernetes 내부 DNS 이름이 된다.
- `selector.app`은 Deployment Pod label과 같아야 한다.
- 일반적인 HTTP 서비스는 `type: ClusterIP`를 기본으로 사용한다.
- 현재 기존 YAML은 `clusterIP: None`을 사용한다. 이 설정은 Headless Service로, 일반적인 단일 서비스 진입점이 필요하면 `type: ClusterIP`가 더 명확하다.

---

## Gateway Route 추가 템플릿

`shared-server/api-gateway/src/main/resources/application.yml`

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: app-3
              uri: lb://APP-3
              predicates:
                - Path=/api/app-3/**
              filters:
                - StripPrefix=0
```

기존 `routes` 배열에 아래 항목만 추가합니다.

```yaml
            - id: app-3
              uri: lb://APP-3
              predicates:
                - Path=/api/app-3/**
              filters:
                - StripPrefix=0
```

---

## Skaffold 신규 서비스 추가 템플릿

`app-3`를 추가할 때 필요한 최소 변경입니다.

### 1. `build.artifacts` 추가

기존 `artifacts` 배열에 추가합니다.

```yaml
    - image: application-app-3
      context: .
      docker:
        dockerfile: application/app-3/Dockerfile
```

작성 기준:

- `image`는 `deployment.yaml`의 `containers[].image`와 같아야 한다.
- `context: .`는 Gradle 멀티 모듈 전체를 Docker build context로 사용하기 위한 설정이다.
- `dockerfile`은 서비스별 Dockerfile 경로다.

### 2. `manifests.rawYaml` 추가

기존 `rawYaml` 배열에 추가합니다.

```yaml
    - k8s/app-3/*.yaml
```

작성 기준:

- 해당 경로에 `config.yaml`, `deployment.yaml`, `service.yaml`이 있어야 한다.
- Skaffold는 이 파일들을 Kubernetes manifest로 적용한다.

### 3. `portForward` 추가

로컬에서 직접 호출하고 싶다면 기존 `portForward` 배열에 추가합니다.

```yaml
  - resourceType: service
    resourceName: application-app-3
    port: 12000
```

작성 기준:

- `resourceName`은 Kubernetes Service `metadata.name`과 같아야 한다.
- `port`는 Service `spec.ports[].port`와 같아야 한다.

---

## 현재 프로젝트 기준 `skaffold.yaml` 예시

```yaml
apiVersion: skaffold/v4beta14
kind: Config
metadata:
  name: sample-k8s
build:
  artifacts:
    - image: application-app-1
      context: .
      docker:
        dockerfile: application/app-1/Dockerfile
    - image: application-app-2
      context: .
      docker:
        dockerfile: application/app-2/Dockerfile
    - image: shared-server-api-gateway
      context: .
      docker:
        dockerfile: shared-server/api-gateway/Dockerfile
    - image: shared-server-discovery-server
      context: .
      docker:
        dockerfile: shared-server/discovery-server/Dockerfile
manifests:
  rawYaml:
    - k8s/api-gateway/*.yaml
    - k8s/app-1/*.yaml
    - k8s/app-2/*.yaml
    - k8s/discovery-server/*.yaml
portForward:
  - resourceType: service
    resourceName: application-app-1
    port: 10000
  - resourceType: service
    resourceName: application-app-2
    port: 11000
  - resourceType: service
    resourceName: shared-server-api-gateway
    port: 8080
  - resourceType: service
    resourceName: shared-server-discovery-server
    port: 8761
```

---

## 작성 후 검증 명령

Kubernetes YAML 문법 확인:

```bash
kubectl apply --dry-run=client -f k8s/app-3
```

Skaffold 렌더링 확인:

```bash
skaffold render
```

Skaffold 개발 배포:

```bash
skaffold dev
```

배포 상태 확인:

```bash
kubectl get pods
kubectl get svc
kubectl describe pod -l app=application-app-3
kubectl logs -l app=application-app-3 -f
```

Gateway 호출 확인:

```bash
curl http://localhost:8080/api/app-3/test
```

직접 서비스 호출 확인:

```bash
curl http://localhost:12000/api/app-3/test
```

---

## 자주 나는 실수 체크리스트

- `skaffold.yaml`의 `build.artifacts[].image`와 `deployment.yaml`의 `containers[].image`가 다르다.
- `portForward.resourceName`과 `service.yaml`의 `metadata.name`이 다르다.
- `service.yaml`의 `selector.app`과 `deployment.yaml`의 Pod label이 다르다.
- Spring Boot `server.port`와 Deployment `containerPort`, Service `port`가 다르다.
- `DISCOVERY_SERVER_URL`이 실제 Discovery Server Service 이름과 다르다.
- Gateway route의 `lb://APP-NAME`과 Spring `spring.application.name`이 맞지 않는다.
- Dockerfile의 `GRADLE_PROJECT`, `MODULE_DIR`이 실제 Gradle 모듈 경로와 다르다.
- 신규 서비스 YAML 경로를 만들고 `skaffold.yaml`의 `manifests.rawYaml`에 추가하지 않았다.
- 로컬 Skaffold 빌드 이미지를 쓰면서 클러스터가 외부 registry에서 이미지를 pull하려고 한다.
- `skaffold dev` 전에 `skaffold render` 또는 `kubectl apply --dry-run=client`로 manifest를 확인하지 않았다.

---

## 현재 확인할 점

현업 기준으로 안정적인 샘플 프로젝트가 되려면 아래 항목을 정리하는 것이 좋습니다.

- `run_all.sh`, `run_infra.sh`의 Docker Compose 파일명 대소문자 수정
- `app-2` Actuator health YAML 들여쓰기 수정
- Dockerfile 내부 불필요한 `RUN ls -al` 제거
- Service의 `clusterIP: None` 사용 의도 정리. 일반 서비스라면 `type: ClusterIP` 권장
- 운영 환경에서는 이미지 태그를 `latest` 또는 로컬명 대신 registry 경로와 Git SHA 기반 태그로 관리
- Deployment에 readiness/liveness probe와 resource requests/limits 추가
