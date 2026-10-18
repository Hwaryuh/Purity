package kr.hwaryuh.purity.guard

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendCommandsEvent
import com.mojang.brigadier.tree.CommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import kr.hwaryuh.poly.framework.listener.HandleOrder
import kr.hwaryuh.poly.framework.listener.Listener
import kr.hwaryuh.poly.framework.listener.Subscribe
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerCommandPreprocessEvent

@Suppress("UnstableApiUsage")
@Listener
class PaperGuardListener(
    private val filter: GuardFilter,
) {
    @Subscribe(order = HandleOrder.EARLY, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val command = event.message.removePrefix("/")
        if (!filter.blocks(event.player.uniqueId, command, Bukkit.getCommandMap().knownCommands::containsKey)) return
        event.isCancelled = true
        filter.rejection(command).forEach(event.player::sendMessage)
    }

    @Subscribe
    fun onSendCommands(event: AsyncPlayerSendCommandsEvent<CommandSourceStack>) {
        if (!event.isAsynchronous && event.hasFiredAsync()) return
        val root = event.commandNode

        @Suppress("UNCHECKED_CAST")
        val maps = INDEXES.map { it.get(root) as MutableMap<String, CommandNode<CommandSourceStack>> }
        filter.hidden(event.player.uniqueId, maps.first().keys.toSet()).forEach { name ->
            maps.forEach { it.remove(name) }
        }
    }

    private companion object {
        val INDEXES =
            listOf("children", "literals", "arguments").map { name ->
                CommandNode::class.java.getDeclaredField(name).apply { isAccessible = true }
            }
    }
}
