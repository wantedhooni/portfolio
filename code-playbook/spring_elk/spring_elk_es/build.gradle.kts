plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.hibernate.orm") version "7.1.11.Final"
	
}

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


dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")

	// https://mvnrepository.com/artifact/com.agido/logback-elasticsearch-appender
	implementation("com.agido:logback-elasticsearch-appender:3.0.17")

	/*
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	runtimeOnly("com.h2database:h2")
	implementation("org.springframework.boot:spring-boot-h2console")
	// logging 테스트 할려니 미리 뛰워 두어야 한다.
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	*/

	developmentOnly("org.springframework.boot:spring-boot-devtools")


	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	// StringDoc
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

	// TEST
	testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
}

tasks.register("prepareKotlinBuildScriptModel"){}

hibernate {
	enhancement {
		enableAssociationManagement = true
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
