package kr.hwaryuh.purity

import kr.hwaryuh.poly.config.ConfigDocuments
import kr.hwaryuh.poly.framework.di.Bean
import kr.hwaryuh.poly.framework.di.Lifecycle
import kr.hwaryuh.poly.framework.di.Reloadable
import kr.hwaryuh.poly.framework.listener.ListenerRegistry
import kr.hwaryuh.purity.config.LoadedSettings
import kr.hwaryuh.purity.config.Settings
import kr.hwaryuh.purity.config.SettingsFile
import kr.hwaryuh.purity.guard.GuardConfig
import kr.hwaryuh.purity.listener.ConnectionListener
import kr.hwaryuh.purity.listener.ProbeListener
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
    private val listeners: ListenerRegistry,
    private val guard: GuardConfig,
) : Lifecycle,
    Reloadable {
    override val id = RELOAD_ID

    @Volatile
    private var current: LoadedSettings? = null
    val settings: Settings get() = checkNotNull(current) { "Purity is not running" }.settings
    val detectionEnabled: Boolean get() = current?.detectionEnabled == true

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
    private var connectionListener: ConnectionListener? = null
    private var probeListener: ProbeListener? = null

    override fun start() {
        try {
            if (!plugin.dataFolder.resolve("config.yml").exists()) plugin.saveResource("config.yml", false)
            val candidate = settingsFile.loadSnapshot()
            val guardCandidate = guard.read()
            current = candidate
            guard.snapshot = guardCandidate
            if (!candidate.detectionEnabled) return
            tracker = ServiceLoader.load(PayloadTracker::class.java, plugin.javaClass.classLoader).single()
            probe =
                SignProbe(plugin, { inspector.settings }, { player, results ->
                    if (detectionEnabled) inspector.onProbeFinished(player, results)
                })
            inspector = Inspector(plugin.logger, tracker, candidate.settings, probe::results)

            val permission = Permission(BYPASS_PERMISSION, PermissionDefault.FALSE)
            plugin.server.pluginManager.addPermission(permission)
            bypass = permission
            trackerStarted = true
            tracker.install { player ->
                if (detectionEnabled) player.scheduler.run(plugin, { if (detectionEnabled) inspector.recheck(player) }, null)
            }
            connectionListener = ConnectionListener(plugin, this).also(listeners::register)
            probeListener = ProbeListener(this).also(listeners::register)
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
        val candidate = settingsFile.loadSnapshot()
        require(candidate.detectionEnabled == detectionEnabled) { "Restart required to change detection.enabled" }
        val guardCandidate = guard.read()
        if (detectionEnabled) inspector.settings = candidate.settings
        current = candidate
        guard.snapshot = guardCandidate
    }

    override fun stop() {
        current = null
        val cleanups =
            listOf<() -> Unit>(
                { connectionListener?.let(listeners::unregister) },
                { probeListener?.let(listeners::unregister) },
                { if (trackerStarted) tracker.uninstall() },
                { bypass?.let { plugin.server.pluginManager.removePermission(it) } },
            )
        var failure: Throwable? = null
        for (cleanup in cleanups) {
            try {
                cleanup()
            } catch (error: Throwable) {
                val first = failure
                if (first == null) failure = error else if (first !== error) first.addSuppressed(error)
            }
        }
        connectionListener = null
        probeListener = null
        trackerStarted = false
        bypass = null
        failure?.let { throw it }
    }

    companion object {
        const val RELOAD_ID = "purity"
    }
}
