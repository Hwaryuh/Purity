package kr.hwaryuh.purity.guard

import kr.hwaryuh.poly.config.ConfigDocuments
import kr.hwaryuh.poly.framework.di.Bean
import kr.hwaryuh.poly.platform.PolyPlugin

@Bean(binds = [GuardRules::class])
class GuardConfig(
    private val plugin: PolyPlugin,
    private val documents: ConfigDocuments,
) : GuardRules {
    @Volatile
    override var snapshot = GuardSnapshot.of(GuardOptions())

    fun read(): GuardSnapshot {
        val file = plugin.dataDirectory.resolve("guard.yml").toFile()
        val document = documents.read(file)
        document.get("")?.let { require(it is Map<*, *>) { "Expected a mapping: guard.yml" } }
        for (key in listOf("bypass-players", "blocked-commands")) {
            document.get(key)?.let { value ->
                require(value is List<*>) { "Expected a list: $key" }
                require(value.all { it is String }) { "Expected strings: $key" }
            }
        }
        val options = GuardOptionsLoader.load(document)
        val candidate = GuardSnapshot.of(options)
        if (!file.exists()) {
            require(file.parentFile.isDirectory || file.parentFile.mkdirs()) { "Cannot create config directory" }
            GuardOptionsLoader.saveIfNotExists(documents, file, options)
        }
        return candidate
    }
}
