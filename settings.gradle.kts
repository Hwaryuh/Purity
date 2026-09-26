rootProject.name = "Purity"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    ":paper",
    ":velocity",
    ":nms:api",
    ":nms:v26_3",
)
