plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.resource.factory.paper)
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlin.stdlib.jdk8)

    implementation(project(":nms:api"))
    runtimeOnly(project(":nms:v26_3"))

    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
}

tasks.test {
    useJUnitPlatform()
}

val paperLibraryFile: Provider<RegularFile> = layout.buildDirectory.file("generated/paper-library")
val paperLibraryContent: String =
    libs.bundles.paper.library
        .map { it.joinToString("\n") }
        .get()

val generatePaperLibrary =
    tasks.register("generatePaperLibrary") {
        inputs.property("content", paperLibraryContent)
        outputs.file(paperLibraryFile)
        doLast {
            paperLibraryFile
                .get()
                .asFile
                .apply { parentFile.mkdirs() }
                .writeText(paperLibraryContent)
        }
    }

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.shadowJar {
    dependsOn(generatePaperLibrary)
    from(paperLibraryFile)
    archiveBaseName.set("Purity")
    archiveClassifier.set("")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("dist"))
    manifest.attributes("paperweight-mappings-namespace" to "mojang")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

paperPluginYaml {
    name = "Purity"
    version = project.version.toString()
    main = "kr.hwaryuh.purity.Purity"
    loader = "kr.hwaryuh.purity.PurityLoader"
    apiVersion = "26.3"
    authors = listOf("Hwaryuh")
}
