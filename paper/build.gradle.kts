plugins {
    id("poly.paper")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":nms:api"))
    runtimeOnly(project(":nms:v26_3"))

    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("Purity")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("dist"))
    manifest.attributes("paperweight-mappings-namespace" to "mojang")
}

val nmsProject = project(":nms:v26_3")

tasks.named<PolyJarCheck>("polyJarCheck") {
    dependsOn(":nms:v26_3:paperweightUserdevSetup")
    platformClasspath.from(provider { nmsProject.configurations.getByName("mojangMappedServer") })
}

paperPluginYaml {
    name = "Purity"
    version = project.version.toString()
    authors = listOf("Hwaryuh")
}
