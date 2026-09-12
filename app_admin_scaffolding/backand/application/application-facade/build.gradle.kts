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
}


tasks.withType<BootJar> {
    enabled = true
    mainClass.set("com.revy.api.admin.server.ApiAdminServerApplication")
}