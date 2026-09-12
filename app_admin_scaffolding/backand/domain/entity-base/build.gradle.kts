plugins {
    id("java-library")
}
dependencies {
    implementation(project(":common"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.hibernate.orm:hibernate-envers")

    // Ensure JPA API available at compile time
    var jakartaPersistenceVersion = "3.1.0"
    api("jakarta.persistence:jakarta.persistence-api:${jakartaPersistenceVersion}")

    // QueryDSL used in domain
    var querydslVersion = "5.1.0"
    api("com.querydsl:querydsl-jpa:${querydslVersion}:jakarta")
    api("com.querydsl:querydsl-core:${querydslVersion}")

    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("com.querydsl:querydsl-apt") {
        artifact {
            classifier = "jakarta"
        }
    }


}