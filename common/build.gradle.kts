plugins {
    id("poly.common")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(Poly.module("config:velocity"))
    testImplementation(Poly.module("platform:api"))
    testImplementation("org.spongepowered:configurate-yaml:4.2.0")
    testImplementation("net.kyori:adventure-api:${PolyVersions.ADVENTURE}")
}

tasks.test {
    useJUnitPlatform()
}
