rootProject.name = "Purity"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    plugins {
        id("poly.paper") version "0.1"
        id("poly.velocity") version "0.1"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include(
    ":paper",
    ":velocity",
    ":nms:api",
    ":nms:v26_3",
)
