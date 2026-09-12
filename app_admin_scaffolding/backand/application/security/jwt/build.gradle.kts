plugins {
    id("java-library")
}

dependencies {
    implementation(project(":common"))
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework:spring-webmvc")
    api("jakarta.servlet:jakarta.servlet-api")

    var jjwtVersion = "0.12.7"
    // JWT
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")
}
