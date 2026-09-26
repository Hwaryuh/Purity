package kr.hwaryuh.purity.fingerprint

import kr.hwaryuh.purity.detection.Observation
import kr.hwaryuh.purity.detection.Verdict
import kr.hwaryuh.purity.detection.detect
import kr.hwaryuh.purity.detection.evaluate
import kr.hwaryuh.purity.probe.SIGN_LINE_LIMIT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FingerprintsTest {
    @Test
    fun glob() {
        assertTrue(glob("lunarclient*").matches("lunarclient:1.2"))
        assertFalse(glob("fabric*").matches("feather fabric"))
        assertTrue(glob("a.b").matches("a.b"))
        assertFalse(glob("a.b").matches("axb"))
    }

    @Test
    fun catalogIsConsistent() {
        val ids = FINGERPRINTS.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "duplicate ids")
        assertTrue(FINGERPRINTS.all { it.signals.isNotEmpty() })
        assertTrue(PROBES.all { it.key.length <= SIGN_LINE_LIMIT })
    }

    @Test
    fun displayNames() {
        assertEquals("NeoForge", displayName("NEOFORGE"))
        assertEquals("Fabric", displayName("FABRIC"))
        assertEquals("Modded loader", displayName("MODDED_LOADER"))
        // Unknown ids are raw brands or namespaces.
        assertEquals("Johndoe", displayName("JohnDoe"))
    }

    @Test
    fun challengeReplyIsModdedLoader() {
        val o = Observation("vanilla", payloads = setOf("minecraft:brand", "c:version"))
        assertEquals(Verdict(Subject.LOADER, "MODDED_LOADER"), evaluate(detect(FINGERPRINTS, o), emptySet(), kickUnknown = true))
        // A specific loader wins over the generic challenge answer.
        assertEquals(
            Verdict(Subject.LOADER, "NEOFORGE"),
            evaluate(detect(FINGERPRINTS, o.copy(brand = "neoforge")), emptySet(), kickUnknown = true),
        )
    }
}
