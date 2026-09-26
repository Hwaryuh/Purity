package kr.hwaryuh.purity.detection

import kr.hwaryuh.purity.fingerprint.Signal
import kr.hwaryuh.purity.fingerprint.Subject
import kr.hwaryuh.purity.fingerprint.fingerprints
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetectionTest {
    private val catalog =
        fingerprints {
            loader("FABRIC") {
                brand("*fabric*")
                channel("fabric:*")
            }
            client("FEATHER") { brand("feather*") }
            mod("SODIUM") { channel("sodium:*") }
            mod("IRIS") { payload("iris:*") }
            mod("FREECAM") { keybind("key.freecam.toggle") }
        }

    private fun verdict(
        o: Observation,
        allow: Set<String> = emptySet(),
        kickUnknown: Boolean = true,
    ) = evaluate(detect(catalog, o), allow, kickUnknown)

    @Test
    fun brandIsCaseInsensitiveAndKeepsBothDimensions() {
        assertEquals(setOf("FEATHER", "FABRIC"), detect(catalog, Observation("Feather Fabric")).map { it.id }.toSet())
    }

    @Test
    fun everyDetectionIsBlocked() {
        assertNull(verdict(Observation("vanilla")))
        assertNull(verdict(Observation(null)))
        assertEquals(Verdict(Subject.LOADER, "FABRIC"), verdict(Observation("vanilla", channels = setOf("fabric:networking"))))
        // Client sorts before loader.
        assertEquals(Verdict(Subject.CLIENT, "FEATHER"), verdict(Observation("Feather Fabric")))
        assertEquals(Verdict(Subject.MOD, "SODIUM"), verdict(Observation(channels = setOf("sodium:x"))))
    }

    @Test
    fun eachSignalSeesOnlyItsSource() {
        assertEquals(Verdict(Subject.UNKNOWN, "iris"), verdict(Observation(channels = setOf("iris:x"))))
        assertEquals(Verdict(Subject.MOD, "IRIS"), verdict(Observation(payloads = setOf("iris:x"))))
        assertEquals(Verdict(Subject.MOD, "FREECAM"), verdict(Observation(probes = setOf(Signal.Keybind("key.freecam.toggle")))))
    }

    @Test
    fun allowSkipsToNextDetection() {
        assertNull(verdict(Observation("fabric"), allow = setOf("FABRIC")))
        assertEquals(Verdict(Subject.MOD, "SODIUM"), verdict(Observation("fabric", channels = setOf("sodium:x")), allow = setOf("FABRIC")))
    }

    @Test
    fun unexplainedSignalsAreUnknown() {
        val vanilla = Observation("vanilla", payloads = setOf("minecraft:brand"))
        assertNull(verdict(vanilla))
        assertNull(verdict(Observation("Geyser", channels = setOf("floodgate:form"))))
        assertEquals(Verdict(Subject.UNKNOWN, "JohnDoe"), verdict(Observation("JohnDoe")))
        assertEquals(Verdict(Subject.UNKNOWN, "johndoe"), verdict(vanilla.copy(payloads = setOf("johndoe:hello"))))
        // Catalog detections are reported first.
        assertEquals(Verdict(Subject.LOADER, "FABRIC"), verdict(Observation("fabric", channels = setOf("fabric:x", "johndoe:hello"))))
        assertNull(verdict(Observation("JohnDoe"), kickUnknown = false))
    }
}
