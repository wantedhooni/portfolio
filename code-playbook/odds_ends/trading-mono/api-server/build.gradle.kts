import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // Source: https://mvnrepository.com/artifact/com.github.f4b6a3/uuid-creator
    val uuidCreatorVersion = "5.1.0"
    implementation("com.github.f4b6a3:uuid-creator:$uuidCreatorVersion")

    // QueryDsl
    val querydslVersion = "5.1.0"
    implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")

    var jjwtVersion = "0.12.7"
    // JWT
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")


    implementation("org.springframework.boot:spring-boot-starter-restclient")


    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("redis.clients:jedis")
    implementation("io.lettuce:lettuce-core")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    implementation("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}