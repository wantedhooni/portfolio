FROM gradle:9.1-jdk21 AS build
WORKDIR /workspace
COPY gradlew .
COPY gradle gradle
# COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY src src
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system pension \
    && useradd --system --gid pension --home-dir /app pension

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

USER pension:pension

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]