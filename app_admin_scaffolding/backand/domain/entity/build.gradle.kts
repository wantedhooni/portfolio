plugins {
    id("java-library")
}

// Use version variables defined in root build.gradle.kts
dependencies {
    implementation(project(":common"))
    api(project(":domain:entity-base"))

    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("com.querydsl:querydsl-apt") {
        artifact {
            classifier = "jakarta"
        }
    }
}
