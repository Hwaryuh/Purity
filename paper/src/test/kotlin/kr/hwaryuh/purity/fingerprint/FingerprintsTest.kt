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

    // Real trace of a Dawn (Feather) client answering the configuration challenge.
    @Test
    fun dawnTraceIsFullyExplained() {
        val o =
            Observation(
                "Dawn Fabric",
                setOf(
                    "c:register",
                    "c:version",
                    "dawn:client",
                    "dawn_bs:replay_finished",
                    "dawn_bs:replay_started",
                    "fabric-menu-api-v1:open_screen",
                    "fabric:registry/sync",
                    "feather:client",
                    "feather:client/frag",
                ),
                setOf("c:version", "minecraft:brand", "minecraft:register"),
            )
        val evidence = detect(FINGERPRINTS, o)
        assertTrue(evidence.none { it.subject == Subject.UNKNOWN })
        assertEquals(Verdict(Subject.CLIENT, "FEATHER"), evaluate(evidence, emptySet(), kickUnknown = true))
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
