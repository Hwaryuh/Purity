package kr.hwaryuh.purity.listener

import io.papermc.paper.event.packet.UncheckedSignChangeEvent
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import kr.hwaryuh.poly.framework.listener.Subscribe
import kr.hwaryuh.purity.Purity
import org.bukkit.event.player.PlayerQuitEvent

class ProbeListener(
    private val purity: Purity,
) {
    @Subscribe
    fun onClientLoaded(event: PlayerClientLoadedWorldEvent) {
        purity.probe.onClientLoaded(event)
    }

    @Subscribe(ignoreCancelled = true)
    fun onSignChange(event: UncheckedSignChangeEvent) {
        purity.probe.onSignChange(event)
    }

    @Subscribe
    fun onQuit(event: PlayerQuitEvent) {
        purity.probe.onQuit(event)
    }
}
