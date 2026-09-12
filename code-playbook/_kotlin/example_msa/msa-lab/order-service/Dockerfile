# product-service/Dockerfile

ARG JAVA_VERSION=21
ARG APP_PORT=8082
ARG JAR_FILE=build/libs/*.jar

FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon


FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine

ARG APP_PORT
ARG JAR_FILE

WORKDIR /app

COPY --from=builder /app/${JAR_FILE} app.jar

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]