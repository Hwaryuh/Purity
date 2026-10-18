package kr.hwaryuh.purity.listener

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent
import kr.hwaryuh.poly.framework.listener.HandleOrder
import kr.hwaryuh.poly.framework.listener.Listener
import kr.hwaryuh.poly.framework.listener.Subscribe
import kr.hwaryuh.poly.platform.event.PolyReloadEvent
import kr.hwaryuh.purity.Purity
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.plugin.Plugin

@Listener
class ConnectionListener(
    private val plugin: Plugin,
    private val purity: Purity,
) {
    // Fires before the server sends its known packs; the client answers in order, so replies arrive before validation.
    @Subscribe
    fun onInitialConfigure(event: PlayerConnectionInitialConfigureEvent) {
        purity.tracker.challenge(event.connection)
    }

    // Fires at configuration finish with brand, config-phase channels and payloads already known, before the player exists.
    @Subscribe(order = HandleOrder.LATE)
    fun onValidateLogin(event: PlayerConnectionValidateLoginEvent) {
        val connection = event.connection as? PlayerConfigurationConnection ?: return
        if (!event.isAllowed) return
        purity.inspector.check(connection)?.let(event::kickMessage)
    }

    // Only game-phase registrations fire this event.
    @Subscribe
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) {
        purity.inspector.recheck(event.player)
    }

    // Fires before the new options are applied, so recheck on the next tick.
    @Subscribe
    fun onClientOptionsChange(event: PlayerClientOptionsChangeEvent) {
        if (event.hasViewDistanceChanged()) {
            event.player.scheduler.run(
                plugin,
                {
                    purity.inspector.recheck(event.player)
                },
                null,
            )
        }
    }

    @Subscribe
    fun onReload(event: PolyReloadEvent) {
        if (Purity.RELOAD_ID !in event.ids) return
        plugin.server.onlinePlayers.forEach { player ->
            player.scheduler.run(plugin, { purity.inspector.recheck(player) }, null)
        }
    }
}
