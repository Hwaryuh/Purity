package kr.hwaryuh.purity.velocity

import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PurityVelocityTest {
    @Test
    fun onlyMarkedKicksDisconnect() {
        assertTrue(PurityVelocity.isPurityKick(Component.text().insertion(PurityVelocity.KICK_MARKER).append(Component.text("x")).build()))
        assertFalse(PurityVelocity.isPurityKick(Component.text("Server closed")))
    }
}
