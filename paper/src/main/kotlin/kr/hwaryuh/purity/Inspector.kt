package kr.hwaryuh.purity

import com.destroystokyo.paper.ClientOption
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
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import java.util.UUID
import java.util.logging.Logger

// Declared default false in Purity; undeclared permissions default to op, which would exempt every op.
const val BYPASS_PERMISSION = "purity.bypass"

// Tags Purity kicks so the Velocity bridge disconnects instead of redirecting. Must match PurityVelocity.KICK_MARKER.
const val KICK_MARKER = "purity:kick"

class Inspector(
    private val logger: Logger,
    val tracker: PayloadTracker,
    var settings: Settings,
) {
    private val probeResults = HashMap<UUID, Map<Signal.Probe, ProbeResult>>()

    fun probeResults(player: Player): Map<Signal.Probe, ProbeResult> = probeResults[player.uniqueId].orEmpty()

    fun observe(player: Player) =
        Observation(
            player.clientBrandName,
            player.listeningPluginChannels,
            tracker.payloads(player),
            probeResults(player).filterValues { it == ProbeResult.RESOLVED }.keys,
        )

    fun evidence(o: Observation): List<Evidence> = detect(FINGERPRINTS, o)

    fun verdict(evidence: List<Evidence>): Verdict? = evaluate(evidence, settings.allow, settings.kickUnknown)

    // Returns the kick message, or null when the connection may stay.
    fun check(
        name: String?,
        locale: String?,
        o: Observation,
        bypass: Boolean,
        log: Boolean,
    ): Component? {
        val evidence = evidence(o)
        val verdict = verdict(evidence)
        val enforce = verdict != null && !bypass && settings.enforce
        if ((log && settings.logDetections) || enforce) logger.info(describe(name, o, evidence, verdict, bypass))
        if (!enforce) return null
        val message = MiniMessage.miniMessage().deserialize(settings.messages.get(locale, verdict.subject), Placeholder.unparsed("id", verdict.id))
        return Component.text().insertion(KICK_MARKER).append(message).build()
    }

    fun recheck(player: Player) {
        if (!player.isOnline || isBypassed(player)) return
        check(player.name, player.getClientOption(ClientOption.LOCALE), observe(player), bypass = false, log = false)?.let(player::kick)
    }

    fun onProbeFinished(
        player: Player,
        results: Map<Signal.Probe, ProbeResult>,
    ) {
        probeResults[player.uniqueId] = results
        // A clean result is the common case; only log what needs attention.
        val clean = results.values.all { it == ProbeResult.UNRESOLVED }
        if (settings.logDetections && !clean) logger.info(describeProbes(player.name, results))
        recheck(player)
    }

    fun forgetProbes(id: UUID) {
        probeResults.remove(id)
    }

    // e.g. "Kicked Steve: LOADER FABRIC | brand "fabric", channel "fabric:registry/sync" | also MOD SODIUM"
    private fun describe(
        name: String?,
        o: Observation,
        evidence: List<Evidence>,
        verdict: Verdict?,
        bypass: Boolean,
    ): String {
        val main = verdict?.let { "${it.subject} ${it.id}" }
        val head =
            when {
                verdict == null -> "Allowed $name"
                bypass -> "Bypassed $name: $main"
                !settings.enforce -> "Would kick $name (enforce: false): $main"
                else -> "Kicked $name: $main"
            }
        val reasons =
            evidence
                .filter { verdict == null || it.id == verdict.id }
                .map(::reason)
                .distinct()
                .ifEmpty { listOf(o.brand?.let { "brand \"$it\"" } ?: "no brand") }
        val others = evidence.map { "${it.subject} ${it.id}" }.distinct().filter { it != main }
        return buildList {
            add(head)
            add(reasons.joinToString(", "))
            if (others.isNotEmpty()) add((if (verdict == null) "allowed " else "also ") + others.joinToString(", "))
            if (settings.logChannels) add("channels ${o.channels} payloads ${o.payloads}")
        }.joinToString(" | ")
    }

    private fun reason(e: Evidence): String =
        when (e.signal) {
            is Signal.Brand -> "brand \"${e.observed}\""
            is Signal.Channel -> "channel \"${e.observed}\""
            is Signal.Payload -> "payload \"${e.observed}\""
            is Signal.Probe -> "probe ${e.signal}"
        }

    // e.g. "Probed Steve: 1 resolved [keybind:key.freecam.toggle], 27 unresolved, 1 inconclusive"
    private fun describeProbes(
        name: String,
        results: Map<Signal.Probe, ProbeResult>,
    ): String {
        val counts = results.values.groupingBy { it }.eachCount()
        val parts =
            ProbeResult.entries.mapNotNull { result ->
                counts[result]?.let { n ->
                    val keys = if (result == ProbeResult.RESOLVED) " ${results.filterValues { it == result }.keys}" else ""
                    "$n ${result.name.lowercase()}$keys"
                }
            }
        return "Probed $name: ${parts.joinToString(", ")}"
    }

    private fun isBypassed(player: Player) = player.uniqueId in settings.bypass || player.hasPermission(BYPASS_PERMISSION)
}
