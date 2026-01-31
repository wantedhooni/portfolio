plugins {
    id("java-library")
}
dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations:2.19.0")
    // Source: https://mvnrepository.com/artifact/com.github.f4b6a3/uuid-creator
    val uuidCreatorVersion = "5.1.0"
    api("com.github.f4b6a3:uuid-creator:$uuidCreatorVersion")

    // Source: https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    val commonLang3Version = "3.20.0"
    api("org.apache.commons:commons-lang3:$commonLang3Version")
}