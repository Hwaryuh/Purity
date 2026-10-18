package kr.hwaryuh.purity.config

import kr.hwaryuh.poly.config.BukkitConfigDocuments
import kr.hwaryuh.purity.fingerprint.Subject
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolySettingsTest {
    private val bundled = javaClass.classLoader.getResource("config.yml")!!.readText(Charsets.UTF_8)

    @Test
    fun bundledConfigMatchesLegacyLoader() =
        withFile(bundled) { source, _ ->
            val actual = source.load()
            val legacy = Settings.from(YamlConfiguration().apply { loadFromString(bundled) })
            assertEquals(legacy.enforce, actual.enforce)
            assertEquals(legacy.allow, actual.allow)
            assertEquals(legacy.kickUnknown, actual.kickUnknown)
            assertEquals(legacy.bypass, actual.bypass)
            assertEquals(legacy.probesEnabled, actual.probesEnabled)
            assertEquals(legacy.probeTimeoutTicks, actual.probeTimeoutTicks)
            assertEquals(legacy.logDetections, actual.logDetections)
            assertEquals(legacy.logChannels, actual.logChannels)
            for (locale in listOf("ko_kr", "en_us", "KO_KR", "en_gb", "ja_jp")) {
                for (subject in Subject.entries) {
                    assertEquals(legacy.messages.get(locale, subject), actual.messages.get(locale, subject))
                }
            }
        }

    @Test
    fun oldFileUsesDefaultsWithoutRewriting() {
        val text =
            """
            # Keep this comment.
            enforce: false
            allow: [SODIUM]
            unknown: log
            bypass: [00000000-0000-0000-0000-000000000001]
            probes:
              timeout-ticks: 99
            logging:
              detections: false
            messages:
              default: KO_KR
              blocked-client:
                KO_KR: custom
              blocked-loader:
                pt_br: loader-pt
            """.trimIndent()
        withFile(text) { source, file ->
            val settings = source.load()
            assertFalse(settings.enforce)
            assertEquals(setOf("SODIUM"), settings.allow)
            assertFalse(settings.kickUnknown)
            assertEquals(setOf(UUID.fromString("00000000-0000-0000-0000-000000000001")), settings.bypass)
            assertTrue(settings.probesEnabled)
            assertEquals(99L, settings.probeTimeoutTicks)
            assertFalse(settings.logDetections)
            assertFalse(settings.logChannels)
            assertEquals("custom", settings.messages.get("ko_kr", Subject.CLIENT))
            assertEquals("loader-pt", settings.messages.get("pt_pt", Subject.LOADER))
            assertTrue(settings.messages.get("ja_jp", Subject.UNKNOWN).isNotEmpty())
            assertEquals(text, file.readText(Charsets.UTF_8))
        }
    }

    @Test
    fun explicitNullUsesTheSameDefaultsAsAMissingKey() =
        withFile(
            """
            enforce: null
            allow: null
            probes: null
            logging:
              channels: null
            messages:
              blocked-client:
                ko_kr: null
            """.trimIndent(),
        ) { source, _ ->
            val settings = source.load()
            assertTrue(settings.enforce)
            assertEquals(emptySet(), settings.allow)
            assertTrue(settings.probesEnabled)
            assertFalse(settings.logChannels)
            assertTrue(settings.messages.get("ko_kr", Subject.CLIENT).isNotEmpty())
        }

    @Test
    fun invalidValuesAreRejectedWithoutRewriting() {
        val invalid =
            listOf(
                "allow: [VOICECHATT]",
                "unknown: ignore",
                "bypass: [invalid-uuid]",
                "enforce: maybe",
                "allow: SODIUM",
                "bypass: 00000000-0000-0000-0000-000000000001",
                "probes: []",
                "probes: true",
                "probes:\n  enabled: nope",
                "probes:\n  timeout-ticks: none",
                "logging: true",
                "messages: 7",
                "messages:\n  blocked-client: []",
                "messages:\n  blocked-loader: text",
                "enforce: [true]",
                "allow: [null]",
                "broken: [",
            )
        for (text in invalid) {
            withFile(text) { source, file ->
                assertFails(text) { source.load() }
                assertEquals(text, file.readText(Charsets.UTF_8))
            }
        }
    }

    @Test
    fun customDefaultLocaleMustDefineEverySubject() =
        withFile("messages:\n  default: fr_fr\n  blocked-client:\n    fr_fr: custom\n") { source, _ ->
            val failure = assertFailsWith<IllegalArgumentException> { source.load() }
            assertTrue(failure.message.orEmpty().contains("must define every blocked-* message"))
        }

    @Test
    fun lastConfiguredLocaleWinsAfterNormalization() =
        withFile("messages:\n  blocked-client:\n    ko_kr: first\n    KO_KR: last\n") { source, _ ->
            assertEquals("last", source.load().messages.get("ko_kr", Subject.CLIENT))
        }

    private fun withFile(
        text: String,
        check: (SettingsFile, File) -> Unit,
    ) {
        val directory = Files.createTempDirectory("purity-config-")
        val file = directory.resolve("config.yml").toFile()
        try {
            file.writeText(text, Charsets.UTF_8)
            check(SettingsFile(BukkitConfigDocuments(), file, bundled), file)
        } finally {
            Files.deleteIfExists(file.toPath())
            Files.deleteIfExists(directory)
        }
    }
}
