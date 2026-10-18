package kr.hwaryuh.purity.guard

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kr.hwaryuh.poly.command.RESTRICTED_PERMISSION
import kr.hwaryuh.poly.framework.di.Lifecycle
import kr.hwaryuh.poly.framework.listener.HandleOrder
import kr.hwaryuh.poly.framework.listener.Listener
import kr.hwaryuh.poly.framework.listener.Subscribe
import kr.hwaryuh.poly.platform.event.PolyReadyEvent
import kr.hwaryuh.poly.platform.event.PolyReloadEvent
import kr.hwaryuh.purity.Purity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.logging.Level

@Listener
class PaperGuardPermissions(
    private val plugin: Plugin,
    private val rules: GuardRules,
) : Lifecycle {
    private val attachments = Object2ObjectOpenHashMap<UUID, PermissionAttachment>()

    @Subscribe(order = HandleOrder.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        synchronize(event.player, true)
    }

    @Subscribe
    fun onQuit(event: PlayerQuitEvent) {
        synchronize(event.player, false, remove = true)
    }

    @Subscribe
    fun onReady(event: PolyReadyEvent) {
        plugin.server.onlinePlayers.forEach { synchronize(it, true) }
    }

    @Subscribe(order = HandleOrder.FIRST)
    fun onReload(event: PolyReloadEvent) {
        if (Purity.RELOAD_ID !in event.ids) return
        plugin.server.onlinePlayers.forEach { synchronize(it, false) }
    }

    private fun synchronize(
        player: Player,
        refresh: Boolean,
        remove: Boolean = false,
    ) {
        try {
            val bypass = !remove && player.uniqueId in rules.snapshot.bypassPlayers
            val existing = attachments[player.uniqueId]
            if (bypass) {
                if (existing == null) {
                    attachments[player.uniqueId] = player.addAttachment(plugin, RESTRICTED_PERMISSION, true)
                } else {
                    existing.setPermission(RESTRICTED_PERMISSION, true)
                }
            } else if (existing != null) {
                existing.unsetPermission(RESTRICTED_PERMISSION)
                existing.remove()
                attachments.remove(player.uniqueId)
            }
            if (refresh) player.updateCommands()
        } catch (failure: Exception) {
            plugin.logger.log(Level.WARNING, "Restricted permission sync failed: ${player.uniqueId}", failure)
        } finally {
            if (remove) attachments.remove(player.uniqueId)
        }
    }

    override fun stop() {
        attachments.values.toList().forEach { attachment ->
            synchronize(attachment.permissible as Player, false, remove = true)
        }
        attachments.clear()
    }
}
