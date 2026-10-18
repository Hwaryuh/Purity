package kr.hwaryuh.purity

import kr.hwaryuh.poly.config.ConfigDocuments
import kr.hwaryuh.poly.framework.di.Bean
import kr.hwaryuh.poly.framework.di.Lifecycle
import kr.hwaryuh.poly.framework.di.Reloadable
import kr.hwaryuh.purity.config.SettingsFile
import kr.hwaryuh.purity.nms.PayloadTracker
import kr.hwaryuh.purity.probe.SignProbe
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.Plugin
import java.util.ServiceLoader

@Bean
class Purity(
    private val plugin: Plugin,
    documents: ConfigDocuments,
) : Lifecycle,
    Reloadable {
    override val id = RELOAD_ID

    lateinit var tracker: PayloadTracker
        private set
    lateinit var inspector: Inspector
        private set
    lateinit var probe: SignProbe
        private set

    private val settingsFile by lazy {
        val bundled =
            requireNotNull(plugin.getResource("config.yml")) { "Missing bundled config.yml" }
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        SettingsFile(documents, plugin.dataFolder.resolve("config.yml"), bundled)
    }
    private var bypass: Permission? = null
    private var trackerStarted = false

    override fun start() {
        try {
            if (!plugin.dataFolder.resolve("config.yml").exists()) plugin.saveResource("config.yml", false)
            val settings = settingsFile.load()
            tracker = ServiceLoader.load(PayloadTracker::class.java, plugin.javaClass.classLoader).single()
            probe = SignProbe(plugin, { inspector.settings }, { player, results -> inspector.onProbeFinished(player, results) })
            inspector = Inspector(plugin.logger, tracker, settings, probe::results)

            val permission = Permission(BYPASS_PERMISSION, PermissionDefault.FALSE)
            plugin.server.pluginManager.addPermission(permission)
            bypass = permission
            trackerStarted = true
            tracker.install { player -> player.scheduler.run(plugin, { inspector.recheck(player) }, null) }
        } catch (failure: Throwable) {
            try {
                stop()
            } catch (cleanup: Throwable) {
                failure.addSuppressed(cleanup)
            }
            throw failure
        }
    }

    override fun reload() {
        inspector.settings = settingsFile.load()
    }

    override fun stop() {
        try {
            if (trackerStarted) {
                trackerStarted = false
                tracker.uninstall()
            }
        } finally {
            bypass?.let { plugin.server.pluginManager.removePermission(it) }
            bypass = null
        }
    }

    companion object {
        const val RELOAD_ID = "purity"
    }
}
