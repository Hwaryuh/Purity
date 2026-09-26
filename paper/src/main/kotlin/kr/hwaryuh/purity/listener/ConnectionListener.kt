package kr.hwaryuh.purity.listener

import com.destroystokyo.paper.ClientOption
import kr.hwaryuh.purity.Inspector
import kr.hwaryuh.purity.detection.Observation
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRegisterChannelEvent

class ConnectionListener(
    private val inspector: Inspector,
) : Listener {
    // Fires at configuration finish with brand, config-phase channels and payloads already known, before the player exists.
    @EventHandler(priority = EventPriority.HIGH)
    fun onValidateLogin(event: PlayerConnectionValidateLoginEvent) {
        val connection = event.connection as? PlayerConfigurationConnection ?: return
        if (!event.isAllowed) return
        val profile = connection.profile
        val observation =
            Observation(
                connection.clientBrandName,
                connection.listeningPluginChannels,
                inspector.tracker.payloads(connection),
            )
        val bypass = profile.id in inspector.settings.bypass
        inspector
            .check(
                profile.name,
                connection.getClientOption(ClientOption.LOCALE),
                observation,
                bypass,
                log = true,
            )?.let(event::kickMessage)
    }

    // Only game-phase registrations fire this event.
    @EventHandler
    fun onRegisterChannel(event: PlayerRegisterChannelEvent) = inspector.recheck(event.player)
}
