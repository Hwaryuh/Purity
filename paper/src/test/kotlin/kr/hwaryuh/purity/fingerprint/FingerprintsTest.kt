package kr.hwaryuh.purity.fingerprint

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
}
