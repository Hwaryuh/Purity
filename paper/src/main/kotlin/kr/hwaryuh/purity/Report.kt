package kr.hwaryuh.purity

import kr.hwaryuh.purity.detection.Evidence
import kr.hwaryuh.purity.detection.Observation
import kr.hwaryuh.purity.detection.Verdict
import kr.hwaryuh.purity.fingerprint.Signal
import kr.hwaryuh.purity.probe.ProbeResult

// Log and /purity info text. Pure, so it can be tested without a server.

// e.g. "Kicked Steve: LOADER FABRIC | brand "fabric", channel "fabric:registry/sync" | also MOD SODIUM"
internal fun describe(
    name: String?,
    o: Observation,
    evidence: List<Evidence>,
    verdict: Verdict?,
    bypass: Boolean,
    enforce: Boolean,
    withChannels: Boolean,
): String {
    val main = verdict?.let { "${it.subject} ${it.id}" }
    val head =
        when {
            verdict == null -> "Allowed $name"
            bypass -> "Bypassed $name: $main"
            !enforce -> "Would kick $name (enforce: false): $main"
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
        if (withChannels) add("channels ${o.channels} payloads ${o.payloads}")
    }.joinToString(" | ")
}

private fun reason(e: Evidence): String =
    when (e.signal) {
        is Signal.Brand -> "brand \"${e.observed}\""
        is Signal.Channel -> "channel \"${e.observed}\""
        is Signal.Payload -> "payload \"${e.observed}\""
        is Signal.Probe -> "probe ${e.signal}"
        is Signal.ViewDistanceAbove -> "view distance ${e.observed}"
    }

// e.g. "Probed Steve: 1 resolved [keybind:key.freecam.toggle], 27 unresolved, 1 inconclusive"
internal fun describeProbes(
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

// /purity info
internal fun info(
    name: String,
    o: Observation,
    probes: Map<Signal.Probe, ProbeResult>,
    evidence: List<Evidence>,
    verdict: Verdict?,
    enforce: Boolean,
): List<String> =
    buildList {
        add("$name brand=${o.brand}")
        add("channels=${o.channels.sorted()}")
        add("payloads=${o.payloads.sorted()}")
        add("view-distance=${o.viewDistance}")
        probes.forEach { (key, result) -> add("probe $key: $result") }
        evidence.forEach { add(" ${it.subject} ${it.id} via ${it.signal}: ${it.observed}") }
        add("verdict=${verdict ?: "allow"} enforce=$enforce")
    }
