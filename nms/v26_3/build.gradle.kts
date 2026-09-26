plugins {
    alias(libs.plugins.paperweight.userdev)
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
    compileOnly(libs.kotlin.stdlib.jdk8)

    implementation(project(":nms:api"))
}
