package kr.hwaryuh.purity.probe

import io.papermc.paper.event.packet.UncheckedSignChangeEvent
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.Position
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kr.hwaryuh.purity.config.Settings
import kr.hwaryuh.purity.fingerprint.PROBES
import kr.hwaryuh.purity.fingerprint.Signal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.UUID

// Translation-key probe: shows a client-only sign below the world and reads back how the client resolved each line.
// The close packet follows the open in the same flush, so the client opens and closes the editor before rendering a frame.
@Suppress("UnstableApiUsage")
class SignProbe(
    private val plugin: Plugin,
    private val settings: () -> Settings,
    private val onFinished: (Player, Map<Signal.Probe, ProbeResult>) -> Unit,
    private val onQuit: (UUID) -> Unit,
) : Listener {
    private class Run(
        val batches: List<List<Signal.Probe>>,
    ) {
        val results = linkedMapOf<Signal.Probe, ProbeResult>()
        var index = 0
        var position: BlockPosition? = null
        var timeout: ScheduledTask? = null
    }

    private val running = HashMap<UUID, Run>()
    private val probed = HashSet<UUID>()

    // Also fires after respawn and dimension changes, so auto probes run once per session.
    @EventHandler
    fun onClientLoaded(event: PlayerClientLoadedWorldEvent) {
        if (!settings().probesEnabled || event.isTimeout || !probed.add(event.player.uniqueId)) return
        start(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSignChange(event: UncheckedSignChangeEvent) {
        val player = event.player
        val run = running[player.uniqueId] ?: return
        if (event.editedBlockPosition != run.position) return
        event.isCancelled = true
        run.timeout?.cancel()
        val lines = event.lines().map { PlainTextComponentSerializer.plainText().serialize(it) }
        run.results += classifyProbe(run.batches[run.index], lines, CONTROL_KEY)
        run.index++
        send(player, run)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val id = event.player.uniqueId
        running.remove(id)?.timeout?.cancel()
        probed.remove(id)
        onQuit(id)
    }

    // Returns false when a probe is already running.
    fun start(player: Player): Boolean {
        if (player.uniqueId in running) return false
        val run = Run(PROBES.chunked(3))
        running[player.uniqueId] = run
        send(player, run)
        return true
    }

    private fun send(
        player: Player,
        run: Run,
    ) {
        if (!player.isOnline) return
        if (run.index == run.batches.size) return finish(player, run)
        // Opening an editor over another GUI would desync it; give up instead.
        if (player.openInventory.type != InventoryType.CRAFTING) {
            run.batches
                .drop(run.index)
                .flatten()
                .forEach { run.results[it] = ProbeResult.INCONCLUSIVE }
            return finish(player, run)
        }
        val location = player.location.toBlockLocation().apply { y = player.world.minHeight.toDouble() }
        val position = Position.block(location)
        val sign = Material.OAK_SIGN.createBlockData().createBlockState() as Sign
        val side = sign.getSide(Side.FRONT)
        run.batches[run.index].forEachIndexed { i, probe -> side.line(i, component(probe)) }
        side.line(3, Component.keybind(CONTROL_KEY))

        player.sendBlockChange(location, sign.blockData)
        player.sendBlockUpdate(location, sign)
        player.openVirtualSign(position, Side.FRONT)
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
        player.sendBlockChange(location, location.block.blockData)

        run.position = position
        run.timeout =
            player.scheduler.runDelayed(plugin, {
                if (running[player.uniqueId] === run) {
                    run.batches[run.index].forEach { run.results[it] = ProbeResult.INCONCLUSIVE }
                    run.index++
                    send(player, run)
                }
            }, null, settings().probeTimeoutTicks)
    }

    private fun finish(
        player: Player,
        run: Run,
    ) {
        running.remove(player.uniqueId)
        onFinished(player, run.results)
    }

    private fun component(probe: Signal.Probe): Component =
        when (probe) {
            is Signal.Keybind -> Component.keybind(probe.key)
            is Signal.Translation -> Component.translatable(probe.key)
        }

    private companion object {
        const val CONTROL_KEY = "key.forward"
    }
}
