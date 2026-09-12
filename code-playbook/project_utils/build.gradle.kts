plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.revy"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["querydslVersion"] = "5.1.0"
extra["uuidCreatorVersion"] = "5.1.0"
extra["commonLang3Version"] = "3.20.0"

ext {
}
val querydslVersion = rootProject.extra["querydslVersion"] as String
val uuidCreatorVersion = rootProject.extra["uuidCreatorVersion"] as String
val commonLang3Version = rootProject.extra["commonLang3Version"] as String

dependencies {

    implementation("com.github.f4b6a3:uuid-creator:$uuidCreatorVersion")
    // Source: https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:$commonLang3Version")


    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.5.7")


    implementation("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.2.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")



    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

tasks.withType<Test> {
    useJUnitPlatform()
}
