plugins {
	val springBootVersion = "3.5.9"
	java
	id("org.springframework.boot") version springBootVersion
	id("io.spring.dependency-management") version "1.1.7"
	// Spring boot 4.0 이상에서 사용
	// id("org.hibernate.orm") version "7.1.11.Final"
	
}

/*
hibernate {
	enhancement {
		enableAssociationManagement = true
	}
}
 */


group = "com.revy"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }
}

val querydslVersion = "5.1.0"
val jjwtVersion = "0.12.7"
var uuidCreator="6.1.1"
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.projectlombok:lombok:1.18.42")
	runtimeOnly("com.h2database:h2")

	// StringDoc
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

	implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	// implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	// JWT
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
	implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
	runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

	//redis
	implementation("org.springframework.data:spring-data-redis")
	implementation("redis.clients:jedis")
	implementation("io.lettuce:lettuce-core")

	//UUID V7
	// https://mvnrepository.com/artifact/com.github.f4b6a3/uuid-creator
	implementation("com.github.f4b6a3:uuid-creator:$uuidCreator")

	// H2
	// implementation("org.springframework.boot:spring-boot-h2console")
	implementation("com.h2database:h2")

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	// TEST
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
}



tasks.withType<Test> {
	useJUnitPlatform()
}
