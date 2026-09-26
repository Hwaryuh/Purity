package kr.hwaryuh.purity.probe

import kr.hwaryuh.purity.fingerprint.Signal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProbeClassifierTest {
    private val a = Signal.Keybind("key.a")
    private val b = Signal.Translation("mod.b")
    private val c = Signal.Keybind("key.c")
    private val control = "key.forward"

    @Test
    fun classifiesEachLine() {
        assertEquals(
            mapOf(a to ProbeResult.RESOLVED, b to ProbeResult.UNRESOLVED, c to ProbeResult.INCONCLUSIVE),
            classifyProbe(listOf(a, b, c), listOf("G", "mod.b", " ", "W"), control),
        )
    }

    @Test
    fun tamperedControlIsInconclusive() {
        assertTrue(classifyProbe(listOf(a, b, c), listOf("G", "x", "y", control), control).values.all { it == ProbeResult.INCONCLUSIVE })
        assertTrue(classifyProbe(listOf(a, b, c), listOf("G", "x", "y", ""), control).values.all { it == ProbeResult.INCONCLUSIVE })
    }

    @Test
    fun truncatedEchoIsUnresolved() {
        val long = Signal.Translation("k".repeat(SIGN_LINE_LIMIT + 5))
        assertEquals(ProbeResult.UNRESOLVED, classifyProbe(listOf(long), listOf("k".repeat(SIGN_LINE_LIMIT), "", "", "W"), control)[long])
    }
}
