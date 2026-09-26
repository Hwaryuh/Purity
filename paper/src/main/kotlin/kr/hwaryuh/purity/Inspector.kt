package kr.hwaryuh.purity

import com.destroystokyo.paper.ClientOption
import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.connection.PlayerConfigurationConnection
import kr.hwaryuh.purity.config.Settings
import kr.hwaryuh.purity.detection.Evidence
import kr.hwaryuh.purity.detection.Observation
import kr.hwaryuh.purity.detection.Verdict
import kr.hwaryuh.purity.detection.detect
import kr.hwaryuh.purity.detection.evaluate
import kr.hwaryuh.purity.fingerprint.FINGERPRINTS
import kr.hwaryuh.purity.fingerprint.Signal
import kr.hwaryuh.purity.nms.PayloadTracker
import kr.hwaryuh.purity.probe.ProbeResult
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.logging.Logger

// Declared default false in Purity; undeclared permissions default to op, which would exempt every op.
const val BYPASS_PERMISSION = "purity.bypass"

class Inspector(
    private val logger: Logger,
    private val tracker: PayloadTracker,
    var settings: Settings,
    private val probeResults: (Player) -> Map<Signal.Probe, ProbeResult>,
) {
    // Game phase; probes can only have run after join.
    fun observe(player: Player) = observe(player.connection, probeResults(player).filterValues { it == ProbeResult.RESOLVED }.keys)

    private fun observe(
        connection: PlayerCommonConnection,
        probes: Set<Signal.Probe> = emptySet(),
    ) = Observation(
        connection.clientBrandName,
        connection.listeningPluginChannels,
        tracker.payloads(connection),
        probes,
        connection.getClientOption(ClientOption.VIEW_DISTANCE),
    )

    fun evidence(o: Observation): List<Evidence> = detect(FINGERPRINTS, o)

    fun verdict(evidence: List<Evidence>): Verdict? = evaluate(evidence, settings.allow, settings.kickUnknown)

    // Pre-join check. Permissions do not exist yet, so only the bypass UUID list applies.
    fun check(connection: PlayerConfigurationConnection): Component? {
        val profile = connection.profile
        return check(
            profile.name,
            connection.getClientOption(ClientOption.LOCALE),
            observe(connection),
            bypass = profile.id in settings.bypass,
            log = true,
        )
    }

    fun recheck(player: Player) {
        if (!player.isOnline || isBypassed(player)) return
        check(player.name, player.getClientOption(ClientOption.LOCALE), observe(player), bypass = false, log = false)?.let(player::kick)
    }

    // Returns the kick message, or null when the connection may stay.
    private fun check(
        name: String?,
        locale: String?,
        o: Observation,
        bypass: Boolean,
        log: Boolean,
    ): Component? {
        val evidence = evidence(o)
        val verdict = verdict(evidence)
        val enforce = verdict != null && !bypass && settings.enforce
        if ((log && settings.logDetections) || enforce) {
            logger.info(describe(name, o, evidence, verdict, bypass, settings.enforce, settings.logChannels))
        }
        if (!enforce) return null
        return settings.messages.kick(locale, verdict.subject, verdict.id)
    }

    fun onProbeFinished(
        player: Player,
        results: Map<Signal.Probe, ProbeResult>,
    ) {
        // A clean result is the common case; only log what needs attention.
        val clean = results.values.all { it == ProbeResult.UNRESOLVED }
        if (settings.logDetections && !clean) logger.info(describeProbes(player.name, results))
        recheck(player)
    }

    private fun isBypassed(player: Player) = player.uniqueId in settings.bypass || player.hasPermission(BYPASS_PERMISSION)
}
