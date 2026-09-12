

plugins {
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"   // @Configuration 등 open class 자동화
    kotlin("plugin.jpa") version "2.1.21"      // Entity no-arg 생성자 자동 생성
}

group = "com.revy"
version = "0.0.1-SNAPSHOT"
