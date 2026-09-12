plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.6"
    id("java")
    id("java-library")
}

allprojects {
    group = "com.example"
    version = "0.2.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
