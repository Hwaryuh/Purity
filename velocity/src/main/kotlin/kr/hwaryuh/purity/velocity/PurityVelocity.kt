package kr.hwaryuh.purity.velocity

import com.velocitypowered.api.event.player.KickedFromServerEvent
import kr.hwaryuh.poly.framework.listener.HandleOrder
import kr.hwaryuh.poly.framework.listener.Listener
import kr.hwaryuh.poly.framework.listener.Subscribe
import net.kyori.adventure.text.Component

@Listener
class PurityVelocity {
    @Subscribe(order = HandleOrder.MONITOR)
    fun onKicked(event: KickedFromServerEvent) {
        val reason = event.serverKickReason.orElse(null) ?: return
        if (isPurityKick(reason)) {
            event.result = KickedFromServerEvent.DisconnectPlayer.create(reason)
        }
    }

    companion object {
        // Must match KICK_MARKER in the Paper plugin.
        const val KICK_MARKER = "purity:kick"

        @JvmStatic
        fun isPurityKick(reason: Component): Boolean = KICK_MARKER == reason.insertion()
    }
}
