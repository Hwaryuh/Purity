dependencies {
    compileOnly(libs.velocity.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.velocity.api)
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val version = project.version.toString()
    inputs.property("version", version)
    filesMatching("velocity-plugin.json") { expand("version" to version) }
}

tasks.jar {
    archiveBaseName.set("Purity-Velocity")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("dist"))
}
