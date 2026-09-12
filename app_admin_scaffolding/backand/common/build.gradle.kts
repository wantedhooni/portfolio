plugins {
    id("java-library")
}

// Use version variables defined in root build.gradle.kts
dependencies {
    val uuidCreatorVersion = "5.1.0"
    api("com.github.f4b6a3:uuid-creator:$uuidCreatorVersion")
    // Source: https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    val commonLang3Version = "3.20.0"
    api("org.apache.commons:commons-lang3:$commonLang3Version")
}
