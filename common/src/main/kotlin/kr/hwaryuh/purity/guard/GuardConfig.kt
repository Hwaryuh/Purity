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
        val options = GuardOptionsLoader.load(documents.read(file))
        val candidate = GuardSnapshot.of(options)
        if (!file.exists()) {
            require(file.parentFile.isDirectory || file.parentFile.mkdirs()) { "Cannot create config directory" }
            GuardOptionsLoader.saveIfNotExists(documents, file, options)
        }
        return candidate
    }
}
