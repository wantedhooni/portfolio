import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.hibernate.orm") version "7.1.11.Final"
    id("java")
    id("java-library")
}


allprojects {
    group = "com.revy"
    version = "0.1.0"
    repositories {
        mavenCentral()
    }



    // 모든 프로젝트에 공통 Java Toolchain 설정
    plugins.withType<JavaPlugin> {
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }



    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<BootJar> {
        enabled = false
    }

    tasks.withType<Test>().configureEach {
        enabled = false
    }
}


subprojects {
    // Apply dependency management plugin to all subprojects
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.1")
        }
    }

    plugins.withType<JavaPlugin> {
        dependencies {
            // Lombok 공통 선언
            implementation("org.slf4j:slf4j-api:2.0.13")
            compileOnly("org.projectlombok:lombok:1.18.42")
            annotationProcessor("org.projectlombok:lombok:1.18.42")
            testCompileOnly("org.projectlombok:lombok:1.18.42")
            testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

            //
            implementation("org.springframework:spring-context")

            // TEST
            testImplementation("org.junit.jupiter:junit-jupiter")
        }

        // 테스트 공통 설정
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}