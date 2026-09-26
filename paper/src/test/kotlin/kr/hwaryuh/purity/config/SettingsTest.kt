package kr.hwaryuh.purity.config

import org.bukkit.configuration.file.YamlConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SettingsTest {
    private fun load(text: String) = Settings.from(YamlConfiguration().apply { loadFromString(text) })

    @Test
    fun bundledConfigLoads() {
        val settings = load(javaClass.classLoader.getResource("config.yml")!!.readText())
        assertTrue(settings.enforce)
        assertEquals(emptySet(), settings.allow)
    }

    @Test
    fun unknownAllowIdFails() {
        assertFailsWith<IllegalArgumentException> { load("allow: [VOICECHATT]") }
    }
}
