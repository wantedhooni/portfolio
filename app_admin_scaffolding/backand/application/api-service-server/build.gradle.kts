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
    implementation(project(":domain:entity"))
    implementation(project(":application:application-facade"))
    implementation(project(":application:security:jwt"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")

    // log
    // Source: https://mvnrepository.com/artifact/org.codehaus.janino/janino
    implementation("org.codehaus.janino:janino:3.1.12")
    // Source: https://mvnrepository.com/artifact/net.logstash.logback/logstash-logback-encoder
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("redis.clients:jedis")
    implementation("io.lettuce:lettuce-core")

    // Spring Doc
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<BootJar> {
    enabled = true
    mainClass.set("com.revy.api.server.ApiServiceServerApplication")
}