package kr.hwaryuh.purity.velocity.guard

import com.velocitypowered.api.event.command.CommandExecuteEvent
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.proxy.ProxyReloadEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.ServerConnection
import kr.hwaryuh.poly.framework.di.Lifecycle
import kr.hwaryuh.poly.framework.di.Reloader
import kr.hwaryuh.poly.framework.listener.HandleOrder
import kr.hwaryuh.poly.framework.listener.Listener
import kr.hwaryuh.poly.framework.listener.Subscribe
import kr.hwaryuh.purity.guard.GuardFilter
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Listener
class VelocityGuardListener(
    private val proxy: ProxyServer,
    private val filter: GuardFilter,
    private val reloader: Reloader,
) : Lifecycle {
    private class KnownCommands(
        val connection: ServerConnection,
        val names: Set<String>,
    )

    private val known = ConcurrentHashMap<UUID, KnownCommands>()

    @Subscribe
    fun onReload(event: ProxyReloadEvent) {
        reloader.reload("purity")
    }

    @Subscribe(order = HandleOrder.EARLY)
    fun onCommand(event: CommandExecuteEvent) {
        val player = event.commandSource as? Player ?: return
        if (!event.result.isAllowed) return
        val connection = player.currentServer.orElse(null)
        val names = known[player.uniqueId]?.takeIf { it.connection === connection }?.names
        val label = event.command.trimStart().takeWhile { !it.isWhitespace() }.lowercase(Locale.ROOT)
        val local = proxy.commandManager.hasCommand(label)
        val blocked =
            filter.blocks(player.uniqueId, event.command) { name ->
                proxy.commandManager.hasCommand(name) ||
                    (names?.contains(name) ?: (!local && connection != null))
            }
        if (!blocked) return
        event.result = CommandExecuteEvent.CommandResult.denied()
        filter.rejection(event.command).forEach(player::sendMessage)
    }

    @Subscribe
    fun onAvailableCommands(event: PlayerAvailableCommandsEvent) {
        val root = event.rootNode
        val names = root.children.map { it.name }.toSet()
        val connection = event.player.currentServer.orElse(null)
        if (connection == null) {
            known.remove(event.player.uniqueId)
        } else {
            known[event.player.uniqueId] = KnownCommands(connection, names.map { it.lowercase(Locale.ROOT) }.toSet())
        }
        filter.hidden(event.player.uniqueId, names).forEach(root::removeChildByName)
    }

    @Subscribe
    fun onDisconnect(event: DisconnectEvent) {
        known.remove(event.player.uniqueId)
    }

    override fun stop() {
        known.clear()
    }
}
