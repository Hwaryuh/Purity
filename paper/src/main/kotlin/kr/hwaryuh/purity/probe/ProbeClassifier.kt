package kr.hwaryuh.purity.probe

import kr.hwaryuh.purity.fingerprint.Signal

enum class ProbeResult { RESOLVED, UNRESOLVED, INCONCLUSIVE }

// ponytail: mirrors Paper's default -DPaper.maxSignLength; servers overriding it need this changed.
const val SIGN_LINE_LIMIT = 80

// A client without the key echoes it verbatim. controlKey is a vanilla keybind; if it comes back blank or
// unresolved, the client is tampering and nothing is concluded.
fun classifyProbe(
    probes: List<Signal.Probe>,
    lines: List<String>,
    controlKey: String,
): Map<Signal.Probe, ProbeResult> {
    val control = lines.getOrNull(3).orEmpty().trim()
    val tampered = control.isEmpty() || control == controlKey.take(SIGN_LINE_LIMIT)
    return probes.withIndex().associate { (i, probe) ->
        val response = lines.getOrNull(i).orEmpty().trim()
        probe to
            when {
                tampered || response.isEmpty() -> ProbeResult.INCONCLUSIVE
                response == probe.key.take(SIGN_LINE_LIMIT) -> ProbeResult.UNRESOLVED
                else -> ProbeResult.RESOLVED
            }
    }
}
