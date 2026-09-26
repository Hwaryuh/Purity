package kr.hwaryuh.purity.detection

import kr.hwaryuh.purity.fingerprint.Fingerprint
import kr.hwaryuh.purity.fingerprint.Signal
import kr.hwaryuh.purity.fingerprint.Subject
import kotlin.reflect.KClass

data class Evidence(
    val subject: Subject,
    val id: String,
    val signal: Signal,
    val observed: String,
)

data class Verdict(
    val subject: Subject,
    val id: String,
) {
    override fun toString() = "$subject:$id"
}

// probes holds the probe signals the client resolved.
data class Observation(
    val brand: String? = null,
    val channels: Set<String> = emptySet(),
    val payloads: Set<String> = emptySet(),
    val probes: Set<Signal.Probe> = emptySet(),
)

// ponytail: Geyser and Floodgate are assumed benign from their docs; add entries when a real false positive shows up.
private val BENIGN_BRANDS = setOf("vanilla", "geyser")
private val BENIGN_NAMESPACES = setOf("minecraft", "floodgate")

fun detect(
    catalog: List<Fingerprint>,
    o: Observation,
): List<Evidence> {
    val known = match(catalog, o)
    return known + unknown(known, o)
}

private fun match(
    catalog: List<Fingerprint>,
    o: Observation,
): List<Evidence> =
    catalog
        .flatMap { fp ->
            fp.signals.flatMap { signal ->
                val observed =
                    when (signal) {
                        is Signal.Brand -> listOfNotNull(o.brand).filter(signal::matches)
                        is Signal.Channel -> o.channels.filter(signal::matches)
                        is Signal.Payload -> o.payloads.filter(signal::matches)
                        is Signal.Probe -> if (signal in o.probes) listOf(signal.key) else emptyList()
                    }
                observed.map { Evidence(fp.subject, fp.id, signal, it) }
            }
        }.distinct()

// Vanilla sends brand "vanilla" and only minecraft-namespace identifiers; anything else no fingerprint explains is UNKNOWN.
// The id is the raw brand or the identifier's namespace.
private fun unknown(
    known: List<Evidence>,
    o: Observation,
): List<Evidence> {
    val explained = known.map { it.signal::class to it.observed }.toSet()

    fun unexplained(
        values: Set<String>,
        kind: KClass<out Signal>,
        signal: (String) -> Signal,
    ): List<Evidence> =
        values
            .filter { (kind to it) !in explained }
            .map { it to it.substringBefore(':', "minecraft").lowercase() }
            .filter { (_, namespace) -> namespace !in BENIGN_NAMESPACES }
            .map { (value, namespace) -> Evidence(Subject.UNKNOWN, namespace, signal("$namespace:*"), value) }

    val brand =
        o.brand
            ?.takeIf { it.lowercase() !in BENIGN_BRANDS && (Signal.Brand::class to it) !in explained }
            ?.let { Evidence(Subject.UNKNOWN, it, Signal.Brand("*"), it) }
    return listOfNotNull(brand) +
        unexplained(o.channels, Signal.Channel::class, Signal::Channel) +
        unexplained(o.payloads, Signal.Payload::class, Signal::Payload)
}

// Every detection is blocked unless its id is allowed. No detection means no verdict.
// UNKNOWN evidence yields a verdict only when kickUnknown is set.
fun evaluate(
    evidence: List<Evidence>,
    allow: Set<String>,
    kickUnknown: Boolean,
): Verdict? =
    evidence
        .sortedBy { it.subject }
        .firstOrNull { it.id !in allow && (kickUnknown || it.subject != Subject.UNKNOWN) }
        ?.let { Verdict(it.subject, it.id) }
