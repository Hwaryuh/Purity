import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.resource.factory.paper) apply false
    alias(libs.plugins.paperweight.userdev) apply false
}

subprojects {
    if (!buildFile.exists()) return@subprojects

    group = "kr.hwaryuh"
    version = "0.1"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }
}

tasks.register("buildPlugin") {
    group = "build"
    description = "Builds the Paper plugin jar and the Velocity bridge jar."
    dependsOn(":paper:shadowJar", ":velocity:jar")
}

defaultTasks("buildPlugin")
