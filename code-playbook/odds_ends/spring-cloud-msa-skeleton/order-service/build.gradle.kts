plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.0")
    }
}

dependencies {
    implementation(project(":common"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-config")

    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
