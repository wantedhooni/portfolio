pluginManagement {
	repositories {
		maven { url = uri("https://repo.spring.io/snapshot") }
		gradlePluginPortal()
	}
}
rootProject.name = "spring_batch"
include(":sample_batch", ":sample_quartz");