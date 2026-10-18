plugins {
    id("poly.velocity")
}

dependencies {
    implementation(project(":common"))
    testImplementation(kotlin("test"))
    testImplementation(libs.velocity.api)
}

tasks.test {
    useJUnitPlatform()
}

velocityPluginJson {
    id = "purity"
    name = "Purity"
    version = project.version.toString()
    authors = listOf("Hwaryuh")
}

tasks.shadowJar {
    archiveBaseName.set("Purity-Velocity")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("dist"))
}
