package kr.hwaryuh.purity.listener

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent
import kr.hwaryuh.purity.Inspector
import kr.hwaryuh.purity.nms.PayloadTracker
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.plugin.Plugin

class ConnectionListener(
    private val plugin: Plugin,
    private val inspector: Inspector,
    private val tracker: PayloadTracker,
) : Listener {
    // Fires before the server sends its known packs; the client answers in order, so replies arrive before validation.
    @EventHandler
    fun onInitialConfigure(event: PlayerConnectionInitialConfigureEvent) = tracker.challenge(event.connection)

    // Fires at configuration finish with brand, config-phase channels and payloads already known, before the player exists.
    @EventHandler(priority = EventPriority.HIGH)
    fun onValidateLogin(event: PlayerConnectionValidateLoginEvent) {
        val connection = event.connection as? PlayerConfigurationConnection ?: return
        if (!event.isAllowed) return
        inspector.check(connection)?.let(event::kickMessage)
    }

    // Only game-phase registrations fire this event.
    @EventHandler
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) = inspector.recheck(event.player)

    // Fires before the new options are applied, so recheck on the next tick.
    @EventHandler
    fun onClientOptionsChange(event: PlayerClientOptionsChangeEvent) {
        if (event.hasViewDistanceChanged()) event.player.scheduler.run(plugin, { inspector.recheck(event.player) }, null)
    }
}
