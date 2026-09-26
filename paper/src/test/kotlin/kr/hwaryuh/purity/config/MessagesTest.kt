package kr.hwaryuh.purity.config

import kr.hwaryuh.purity.fingerprint.Subject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MessagesTest {
    private val en = Subject.entries.associateWith { "en-$it" }
    private val messages =
        Messages(
            "en_us",
            mapOf(
                "en_us" to en,
                "ko_kr" to mapOf(Subject.CLIENT to "ko-client"),
                "pt_br" to mapOf(Subject.MOD to "pt-mod"),
            ),
        )

    @Test
    fun lookupOrder() {
        assertEquals("ko-client", messages.get("ko_kr", Subject.CLIENT))
        assertEquals("ko-client", messages.get("KO_KR", Subject.CLIENT))
        // Missing key falls back to default.
        assertEquals("en-LOADER", messages.get("ko_kr", Subject.LOADER))
        // Same language, different region.
        assertEquals("pt-mod", messages.get("pt_pt", Subject.MOD))
        assertEquals("en-CLIENT", messages.get("ja_jp", Subject.CLIENT))
        assertEquals("en-CLIENT", messages.get(null, Subject.CLIENT))
    }

    @Test
    fun kickIsMarkedForVelocity() {
        val kick = messages.kick("en_us", Subject.MOD, "SODIUM")
        assertEquals(Messages.KICK_MARKER, kick.insertion())
    }

    @Test
    fun defaultMustBeComplete() {
        assertFailsWith<IllegalArgumentException> { Messages("en_us", mapOf("en_us" to mapOf(Subject.CLIENT to "x"))) }
    }
}
