package kr.hwaryuh.purity

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import kr.hwaryuh.purity.command.PurityCommand
import kr.hwaryuh.purity.config.Settings
import kr.hwaryuh.purity.listener.ConnectionListener
import kr.hwaryuh.purity.nms.PayloadTracker
import kr.hwaryuh.purity.probe.SignProbe
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import java.util.ServiceLoader

class Purity : JavaPlugin() {
    private val tracker = ServiceLoader.load(PayloadTracker::class.java, javaClass.classLoader).single()
    private val bypass = Permission(BYPASS_PERMISSION, PermissionDefault.FALSE)

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.addPermission(bypass)
        lateinit var inspector: Inspector
        val probe = SignProbe(this, { inspector.settings }, { player, results -> inspector.onProbeFinished(player, results) })
        inspector = Inspector(logger, tracker, loadSettings(), probe::results)
        val reload = {
            reloadConfig()
            inspector.settings = loadSettings()
            server.onlinePlayers.forEach(inspector::recheck)
        }
        tracker.install { player -> player.scheduler.run(this, { inspector.recheck(player) }, null) }
        server.pluginManager.registerEvents(ConnectionListener(this, inspector, tracker), this)
        server.pluginManager.registerEvents(probe, this)
        lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS,
        ) { it.registrar().register(PurityCommand(inspector, probe, reload).build()) }
    }

    // Keys missing from a config.yml saved by an older version fall back to the bundled defaults, in memory only.
    private fun loadSettings(): Settings {
        config.options().copyDefaults(true)
        return Settings.from(config)
    }

    override fun onDisable() {
        tracker.uninstall()
        server.pluginManager.removePermission(bypass)
    }
}
