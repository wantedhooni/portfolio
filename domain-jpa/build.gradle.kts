plugins {
    id("java-library")
}

// Use version variables defined in root build.gradle.kts
val querydslVersion: String by rootProject.extra
val jakartaPersistenceVersion: String by rootProject.extra
val jjwtVersion: String by rootProject.extra

dependencies {
    implementation(project(":common"))


    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // Ensure JPA API available at compile time (version from root)
    api("jakarta.persistence:jakarta.persistence-api:${jakartaPersistenceVersion}")

    // QueryDSL used in domain (version from root)
    api("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
    api("com.querydsl:querydsl-core:${querydslVersion}")
    annotationProcessor("com.querydsl:querydsl-apt:${querydslVersion}:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:${jakartaPersistenceVersion}")
}
