import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    id("poly.paper") apply false
    id("poly.velocity") apply false
    alias(libs.plugins.paperweight.userdev) apply false
}

subprojects {
    if (!buildFile.exists()) return@subprojects

    group = "kr.hwaryuh.purity"
    version = "0.1"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }
}

tasks.register("buildPlugin") {
    group = "build"
    description = "Builds the Paper plugin jar and the Velocity bridge jar."
    dependsOn(":paper:shadowJar", ":velocity:shadowJar")
}

defaultTasks("buildPlugin")
