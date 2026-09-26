package kr.hwaryuh.purity.config

import kr.hwaryuh.purity.fingerprint.Subject
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
    fun olderConfigFallsBackToBundledDefaults() {
        val defaults = YamlConfiguration().apply { loadFromString(javaClass.classLoader.getResource("config.yml")!!.readText()) }
        val old =
            YamlConfiguration().apply {
                loadFromString("messages:\n  default: ko_kr\n  blocked-client:\n    ko_kr: old\n")
                setDefaults(defaults)
                options().copyDefaults(true)
            }
        val messages = Settings.from(old).messages
        assertEquals("old", messages.get("ko_kr", Subject.CLIENT))
        assertTrue(messages.get("ko_kr", Subject.UNKNOWN).isNotEmpty())
    }

    @Test
    fun unknownAllowIdFails() {
        assertFailsWith<IllegalArgumentException> { load("allow: [VOICECHATT]") }
    }
}
