plugins {
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"

	kotlin("jvm") version "2.2.0"
	kotlin("plugin.spring") version "2.2.0"
	kotlin("plugin.jpa") version "2.2.0"
}

group = "com.revy"
version = "1.0.0-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")


	runtimeOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

tasks.test {
	useJUnitPlatform()
}