# skaffold-k8s-helm

Spring Boot 기반 MSA 예제를 Docker, Kubernetes, Helm, Skaffold로 빌드하고 배포하는 샘플 프로젝트입니다.

이 프로젝트의 핵심 목적은 다음 흐름을 한 번에 이해하는 것입니다.

1. Gradle 멀티 모듈로 여러 Spring Boot 서비스를 관리한다.
2. 각 서비스를 Docker 이미지로 빌드한다.
3. Kubernetes 리소스는 Helm Chart로 정의한다.
4. Skaffold로 이미지 빌드, Helm 배포, port-forward를 자동화한다.

---

## 전체 구조

```text
skaffold-k8s-helm
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
├── docker
│   └── postgresql
│       └── init.sql
├── k8s
│   └── helm
│       ├── api-gateway
│       ├── app-1
│       ├── app-2
│       └── discovery-server
├── docker-compose-Infra.yml
├── docker-compose-app.yml
├── skaffold.env
├── skaffold.yaml
├── settings.gradle
├── build.gradle
└── gradle.properties
```

---

## 모듈 구성

### `core:common-webmvc`

공통 WebMVC 의존성을 모아둔 라이브러리 모듈입니다.

주요 의존성:

- Spring Boot Actuator
- SpringDoc OpenAPI WebMVC UI
- Eureka Client

`app-1`, `app-2`가 이 모듈을 의존합니다.

### `shared-server:discovery-server`

Eureka Server 역할을 하는 서비스 디스커버리 서버입니다.

- 기본 포트: `8761`
- Spring Cloud Netflix Eureka Server 사용
- 각 서비스가 자신의 위치를 등록하는 서비스 레지스트리 역할

### `shared-server:api-gateway`

외부 요청을 내부 서비스로 라우팅하는 API Gateway입니다.

- 기본 포트: `8080`
- Spring Cloud Gateway WebFlux 사용
- Eureka Client를 통해 `app-1`, `app-2` 위치 조회

현재 라우팅:

| 외부 요청 경로 | 대상 서비스 |
| --- | --- |
| `/api/app-1/**` | `APP-1` |
| `/api/app-2/**` | `APP-2` |

### `application:app-1`

샘플 비즈니스 서비스 1번입니다.

- 기본 포트: `10000`
- Spring WebMVC 기반
- Eureka Client로 Discovery Server에 등록
- 테스트 API: `GET /api/app-1/test`
- 응답 예시: `app_1 Hello World`

### `application:app-2`

샘플 비즈니스 서비스 2번입니다.

- 기본 포트: `11000`
- Spring WebMVC 기반
- Eureka Client로 Discovery Server에 등록
- 테스트 API: `GET /api/app-2/test`
- 응답 예시: `app_2 Hello World`

---

## 서비스 포트

| 서비스 | 포트 | 설명 |
| --- | ---: | --- |
| `api-gateway` | `8080` | 외부 진입점 |
| `discovery-server` | `8761` | Eureka Server |
| `app-1` | `10000` | 샘플 애플리케이션 1 |
| `app-2` | `11000` | 샘플 애플리케이션 2 |
| `postgres` | `5432` | 로컬 PostgreSQL |
| `otel-lgtm` | `3000` | Grafana UI |
| `otel-lgtm` | `4317` | OTLP gRPC |
| `otel-lgtm` | `4318` | OTLP HTTP |

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
| Kubernetes 배포 | Helm |
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

Gateway를 통한 호출 예시:

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

| 서비스 | Dockerfile |
| --- | --- |
| `api-gateway` | `shared-server/api-gateway/Dockerfile` |
| `discovery-server` | `shared-server/discovery-server/Dockerfile` |
| `app-1` | `application/app-1/Dockerfile` |
| `app-2` | `application/app-2/Dockerfile` |

Dockerfile은 공통적으로 다음 방식입니다.

1. `eclipse-temurin:21-jdk` 이미지에서 Gradle `bootJar`를 실행한다.
2. 생성된 JAR 파일을 `eclipse-temurin:21-jre` 런타임 이미지로 복사한다.
3. `java -jar app.jar`로 서비스를 실행한다.

---

## Helm 구성

Helm Chart 위치:

```text
k8s/helm/api-gateway
k8s/helm/discovery-server
k8s/helm/app-1
k8s/helm/app-2
```

각 Chart는 공통적으로 다음 리소스를 생성합니다.

- `ConfigMap`
- `Deployment`
- `Service`

현재 서비스 디스커버리 URL은 Kubernetes Service 이름 기준으로 다음 값이 사용됩니다.

```text
http://discovery-server:8761/eureka/
```

수동 Helm 배포:

```bash
helm upgrade --install discovery-server ./k8s/helm/discovery-server --namespace default --create-namespace
helm upgrade --install api-gateway ./k8s/helm/api-gateway --namespace default --create-namespace
helm upgrade --install app-1 ./k8s/helm/app-1 --namespace default --create-namespace
helm upgrade --install app-2 ./k8s/helm/app-2 --namespace default --create-namespace
```

배포 확인:

```bash
kubectl get pods
kubectl get svc
helm list
```

삭제:

```bash
helm uninstall api-gateway
helm uninstall discovery-server
helm uninstall app-1
helm uninstall app-2
```

---

## Skaffold 구성

`skaffold.yaml`은 4개 서비스를 모두 빌드하고 Helm으로 배포합니다.

### 이미지 빌드 대상

| 이미지 | Dockerfile |
| --- | --- |
| `skaffold-k8s-helm/api-gateway:latest` | `shared-server/api-gateway/Dockerfile` |
| `skaffold-k8s-helm/discovery-server:latest` | `shared-server/discovery-server/Dockerfile` |
| `skaffold-k8s-helm/app-1:latest` | `application/app-1/Dockerfile` |
| `skaffold-k8s-helm/app-2:latest` | `application/app-2/Dockerfile` |

### Helm 배포 대상

| Release | Chart |
| --- | --- |
| `api-gateway` | `k8s/helm/api-gateway` |
| `discovery-server` | `k8s/helm/discovery-server` |
| `app-1` | `k8s/helm/app-1` |
| `app-2` | `k8s/helm/app-2` |

### Port Forward

`skaffold dev` 실행 시 다음 서비스 포트가 로컬로 포워딩됩니다.

| Kubernetes Service | 로컬 포트 |
| --- | ---: |
| `api-gateway` | `8080` |
| `discovery-server` | `8761` |
| `app-1` | `10000` |
| `app-2` | `11000` |

### 실행 명령

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

---

## 권장 실행 순서

처음 실행하는 경우 다음 순서로 확인하는 것이 좋습니다.

### 1. 빌드 확인

```bash
./gradlew clean build -x test
```

### 2. 로컬 Docker Compose 실행 확인

```bash
docker compose -f docker-compose-app.yml up --build
```

API 확인:

```bash
curl http://localhost:8080/api/app-1/test
curl http://localhost:8080/api/app-2/test
```

### 3. Kubernetes 배포 확인

Kubernetes 클러스터, Docker, Helm, Skaffold가 준비되어 있다면 다음을 실행합니다.

```bash
skaffold dev
```

다른 터미널에서 확인:

```bash
kubectl get pods
kubectl get svc
curl http://localhost:8080/api/app-1/test
curl http://localhost:8080/api/app-2/test
```

---

## 현재 확인할 점

현업 기준으로 안정적인 샘플 프로젝트가 되려면 아래 항목을 추가로 정리하는 것이 좋습니다.

- `READEME.md`는 오타 파일명이므로 `README.md`로 통합하거나 제거
- `run_all.sh`, `run_infra.sh`의 Docker Compose 파일명 대소문자 수정
- `app-2` Actuator health YAML 들여쓰기 확인
- Dockerfile 내부 불필요한 `RUN ls -al` 제거
- 운영 환경에서는 이미지 태그를 `latest`가 아닌 버전 또는 Git SHA 기반으로 관리
- Helm Chart 중복이 많으므로 공통 Chart 또는 helper template 적용 검토

---

## Helm / Skaffold 작성 가이드

이 섹션은 새 서비스를 추가할 때 복사해서 사용할 수 있는 작성 순서와 템플릿입니다.

예시는 `app-3` 서비스를 추가한다고 가정합니다.

---

## 새 서비스 추가 작성 순서

실무에서는 아래 순서로 작성하면 누락을 줄일 수 있습니다.

1. Spring Boot 모듈을 추가한다.
2. `settings.gradle`에 Gradle 모듈을 등록한다.
3. 서비스 `application.yml`에 `spring.application.name`, `server.port`, Eureka URL을 설정한다.
4. 서비스용 `Dockerfile`을 작성한다.
5. `k8s/helm/{서비스명}` Chart를 만든다.
6. `values.yaml`에 이미지명, 포트, replica 수를 정의한다.
7. `ConfigMap`에 런타임 환경변수를 정의한다.
8. `Deployment`에서 이미지, 포트, 환경변수 참조를 연결한다.
9. `Service`에서 Kubernetes 내부 접근 이름과 포트를 연다.
10. `skaffold.yaml`의 `build.artifacts`에 Docker 이미지 빌드 대상을 추가한다.
11. `skaffold.yaml`의 `manifests.helm.releases`에 Helm release를 추가한다.
12. 필요하면 `skaffold.yaml`의 `portForward`에 로컬 포트 연결을 추가한다.
13. `skaffold render`로 YAML 렌더링 결과를 먼저 확인한다.
14. `skaffold dev` 또는 `skaffold run`으로 실제 배포한다.

---

## 이름 규칙 권장안

이름을 일관되게 맞추는 것이 가장 중요합니다.

| 항목 | 권장 값 예시 | 설명 |
| --- | --- | --- |
| Gradle module | `:application:app-3` | Gradle 빌드 대상 |
| Spring app name | `app-3` | Eureka에 등록되는 서비스명 |
| Docker image | `skaffold-k8s-helm/app-3` | Skaffold 빌드 이미지 |
| Helm release | `app-3` | Helm 배포 단위 |
| Helm chart name | `app-3` | Kubernetes 리소스 이름 기준 |
| Kubernetes Service | `app-3` | 클러스터 내부 DNS 이름 |
| ConfigMap | `app-3-config` | 환경변수 저장 |
| container name | `app-3` | Pod 내부 컨테이너 이름 |

Spring Cloud Gateway에서 Eureka 기반 라우팅을 할 때는 보통 Spring application name이 대문자로 인식됩니다.

```yaml
uri: lb://APP-3
```

---

## Dockerfile 템플릿

현재 프로젝트의 Dockerfile은 Gradle 멀티 모듈 프로젝트 전체를 Docker build context로 복사한 뒤, 특정 모듈의 `bootJar`만 빌드하는 방식입니다.

`application/app-3/Dockerfile` 예시:

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

shared server 모듈이면 `GRADLE_PROJECT`, `MODULE_DIR`만 바꿉니다.

```dockerfile
ARG GRADLE_PROJECT=:shared-server:some-server
ARG MODULE_DIR=shared-server/some-server
```

---

## Helm Chart 디렉터리 템플릿

새 서비스는 아래 구조로 Chart를 만듭니다.

```text
k8s/helm/app-3
├── Chart.yaml
├── values.yaml
└── templates
    ├── config.yaml
    ├── deployment.yaml
    └── service.yaml
```

생성 명령 예시:

```bash
mkdir -p k8s/helm/app-3/templates
```

---

## Helm `Chart.yaml` 템플릿

`k8s/helm/app-3/Chart.yaml`

```yaml
apiVersion: v2
name: app-3
description: A Helm chart for app-3
type: application
version: 1.0.0
appVersion: 1.0.0
```

작성 기준:

- `name`은 Kubernetes 리소스 이름의 기준으로 사용한다.
- `version`은 Chart 자체 버전이다.
- `appVersion`은 애플리케이션 버전이다.

---

## Helm `values.yaml` 템플릿

`k8s/helm/app-3/values.yaml`

```yaml
replicaCount: 1

image:
  name: app-3
  repository: skaffold-k8s-helm/app-3
  tag: latest

service:
  type: ClusterIP
  port: 12000
  containerPort: 12000
```

작성 기준:

- `image.repository`는 `skaffold.yaml`의 `build.artifacts[].image`와 반드시 맞춘다.
- `service.port`는 Kubernetes Service가 노출하는 포트다.
- `service.containerPort`는 Spring Boot `server.port`와 맞춘다.
- 로컬 개발용이면 `tag: latest`도 가능하지만, 운영에서는 Git SHA 또는 버전 태그를 권장한다.

---

## Helm `config.yaml` 템플릿

`k8s/helm/app-3/templates/config.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: "{{ .Chart.Name }}-config"
data:
  DISCOVERY_SERVER_URL: http://discovery-server:8761/eureka/
```

작성 기준:

- `DISCOVERY_SERVER_URL`은 Kubernetes Service 이름을 사용한다.
- 현재 프로젝트의 Discovery Server Service 이름은 `discovery-server`다.
- 환경별 값이 달라질 수 있으면 `values.yaml`로 분리하는 것이 더 좋다.

환경변수를 `values.yaml`로 분리하고 싶다면 아래 방식도 가능하다.

`values.yaml`

```yaml
env:
  DISCOVERY_SERVER_URL: http://discovery-server:8761/eureka/
```

`templates/config.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: "{{ .Chart.Name }}-config"
data:
  DISCOVERY_SERVER_URL: {{ .Values.env.DISCOVERY_SERVER_URL | quote }}
```

---

## Helm `deployment.yaml` 템플릿

`k8s/helm/app-3/templates/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ .Chart.Name }}
  labels:
    app: {{ .Chart.Name }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      app: {{ .Chart.Name }}
  template:
    metadata:
      labels:
        app: {{ .Chart.Name }}
    spec:
      containers:
        - name: {{ .Values.image.name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: IfNotPresent
          envFrom:
            - configMapRef:
                name: "{{ .Chart.Name }}-config"
          ports:
            - name: http
              containerPort: {{ .Values.service.containerPort }}
```

작성 기준:

- `metadata.name`, `selector.matchLabels`, `template.metadata.labels`, `Service.selector`는 같은 라벨을 사용해야 한다.
- `image`는 `values.yaml`에서 조립한다.
- 로컬 클러스터에서 Skaffold로 직접 빌드한 이미지를 쓸 때는 `imagePullPolicy: IfNotPresent`가 안전하다.
- 운영에서는 readiness/liveness probe, resource requests/limits를 추가하는 것이 좋다.

운영에 가까운 probe 포함 예시:

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

## Helm `service.yaml` 템플릿

`k8s/helm/app-3/templates/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: {{ .Chart.Name }}
  labels:
    app: {{ .Chart.Name }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - name: http
      port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.containerPort }}
  selector:
    app: {{ .Chart.Name }}
```

작성 기준:

- `metadata.name`이 클러스터 내부 DNS 이름이 된다.
- 다른 Pod에서는 `http://app-3:12000`처럼 접근할 수 있다.
- `selector.app`은 Deployment Pod label과 반드시 같아야 한다.

---

## Skaffold 작성 순서

`skaffold.yaml`은 크게 4개 블록으로 작성합니다.

```yaml
apiVersion: skaffold/v4beta14
kind: Config
metadata:
  name: sample-k8s

build:
# Docker 이미지 빌드 설정

manifests:
# Kubernetes manifest 또는 Helm release 설정

deploy:
# 실제 배포 방식 설정

portForward:
# 로컬 개발용 포트 포워딩 설정
```

작성 순서:

1. `metadata.name`으로 Skaffold 설정 이름을 정한다.
2. `build.tagPolicy`로 이미지 태그 정책을 정한다.
3. `build.artifacts`에 서비스별 Docker 이미지 빌드 대상을 추가한다.
4. `manifests.helm.releases`에 배포할 Helm Chart를 추가한다.
5. `deploy.helm`을 활성화한다.
6. 개발 중 직접 호출할 서비스는 `portForward`에 추가한다.
7. `skaffold render`로 Helm 렌더링 결과를 검증한다.
8. `skaffold dev`로 파일 변경 감지 기반 개발 배포를 실행한다.

---

## Skaffold 서비스 추가 템플릿

`app-3`를 추가할 때 필요한 최소 변경 예시입니다.

### 1. `build.artifacts` 추가

```yaml
build:
  tagPolicy:
    envTemplate:
      template: "latest"
  artifacts:
    - image: skaffold-k8s-helm/app-3
      context: .
      docker:
        dockerfile: application/app-3/Dockerfile
```

현재 프로젝트처럼 여러 서비스가 있으면 기존 `artifacts` 배열에 항목만 추가합니다.

```yaml
    - image: skaffold-k8s-helm/app-3
      context: .
      docker:
        dockerfile: application/app-3/Dockerfile
```

작성 기준:

- `image`는 Helm `values.yaml`의 `image.repository`와 같아야 한다.
- `context: .`는 루트 Gradle 멀티 모듈 전체를 Docker build context로 전달하기 위한 설정이다.
- `dockerfile`은 서비스별 Dockerfile 경로다.

### 2. `manifests.helm.releases` 추가

```yaml
manifests:
  helm:
    releases:
      - name: app-3
        chartPath: k8s/helm/app-3
        valuesFiles:
          - k8s/helm/app-3/values.yaml
```

현재 프로젝트처럼 여러 서비스가 있으면 기존 `releases` 배열에 항목만 추가합니다.

```yaml
      - name: app-3
        chartPath: k8s/helm/app-3
        valuesFiles:
          - k8s/helm/app-3/values.yaml
```

작성 기준:

- `name`은 Helm release 이름이다.
- `chartPath`는 Chart 디렉터리다.
- `valuesFiles`는 Chart에 적용할 설정 파일이다.

### 3. `portForward` 추가

```yaml
portForward:
  - resourceType: service
    resourceName: app-3
    port: 12000
```

현재 프로젝트처럼 여러 서비스가 있으면 기존 `portForward` 배열에 항목만 추가합니다.

```yaml
  - resourceType: service
    resourceName: app-3
    port: 12000
```

작성 기준:

- `resourceName`은 Kubernetes Service 이름과 같아야 한다.
- 현재 Helm Chart에서는 Service 이름이 `{{ .Chart.Name }}`이므로 Chart 이름과 같다.
- `port`는 Service port다.

---

## 현재 프로젝트 기준 `skaffold.yaml` 템플릿

아래는 현재 프로젝트 구조에 맞춘 전체 형태입니다.

```yaml
apiVersion: skaffold/v4beta14
kind: Config
metadata:
  name: sample-k8s

build:
  tagPolicy:
    envTemplate:
      template: "latest"
  artifacts:
    - image: skaffold-k8s-helm/api-gateway
      context: .
      docker:
        dockerfile: shared-server/api-gateway/Dockerfile
    - image: skaffold-k8s-helm/discovery-server
      context: .
      docker:
        dockerfile: shared-server/discovery-server/Dockerfile
    - image: skaffold-k8s-helm/app-1
      context: .
      docker:
        dockerfile: application/app-1/Dockerfile
    - image: skaffold-k8s-helm/app-2
      context: .
      docker:
        dockerfile: application/app-2/Dockerfile

manifests:
  helm:
    releases:
      - name: api-gateway
        chartPath: k8s/helm/api-gateway
        valuesFiles:
          - k8s/helm/api-gateway/values.yaml
      - name: discovery-server
        chartPath: k8s/helm/discovery-server
        valuesFiles:
          - k8s/helm/discovery-server/values.yaml
      - name: app-1
        chartPath: k8s/helm/app-1
        valuesFiles:
          - k8s/helm/app-1/values.yaml
      - name: app-2
        chartPath: k8s/helm/app-2
        valuesFiles:
          - k8s/helm/app-2/values.yaml

deploy:
  helm: {}

portForward:
  - resourceType: service
    resourceName: app-1
    port: 10000
  - resourceType: service
    resourceName: app-2
    port: 11000
  - resourceType: service
    resourceName: api-gateway
    port: 8080
  - resourceType: service
    resourceName: discovery-server
    port: 8761
```

---

## 작성 후 검증 명령

Helm Chart 문법 확인:

```bash
helm lint k8s/helm/app-3
```

Helm 템플릿 렌더링 확인:

```bash
helm template app-3 k8s/helm/app-3 -f k8s/helm/app-3/values.yaml
```

Skaffold 전체 렌더링 확인:

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
kubectl describe pod -l app=app-3
kubectl logs -l app=app-3 -f
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

- `skaffold.yaml`의 `build.artifacts[].image`와 Helm `values.yaml`의 `image.repository`가 다르다.
- `portForward.resourceName`과 Kubernetes Service 이름이 다르다.
- Service `selector`와 Deployment Pod `labels`가 다르다.
- Spring Boot `server.port`와 Helm `service.containerPort`가 다르다.
- `DISCOVERY_SERVER_URL`이 실제 Kubernetes Service 이름과 다르다.
- Gateway route의 `lb://APP-NAME`과 Spring `spring.application.name`이 맞지 않는다.
- Dockerfile의 `GRADLE_PROJECT`, `MODULE_DIR`이 실제 Gradle 모듈 경로와 다르다.
- `latest` 태그를 운영 배포에도 그대로 사용한다.
- `skaffold dev` 전에 `skaffold render` 또는 `helm template`으로 렌더링 결과를 확인하지 않는다.
