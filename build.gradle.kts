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

}

subprojects {
    // 반드시 java 플러그인 또는 application/plugin이 적용된 이후에 설정되도록
    plugins.withType<JavaPlugin> {


        dependencies {

            // Lombok 공통 선언
            compileOnly("org.projectlombok:lombok:1.18.42")
            annotationProcessor("org.projectlombok:lombok:1.18.42")
            testCompileOnly("org.projectlombok:lombok:1.18.42")
            testAnnotationProcessor("org.projectlombok:lombok:1.18.42")
        }

        // 테스트 공통 설정
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }


}
