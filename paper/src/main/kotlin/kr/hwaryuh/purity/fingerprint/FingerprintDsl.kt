package kr.hwaryuh.purity.fingerprint

// name is shown to players in kick messages; id is what config, logs and commands use.
class Fingerprint(
    val id: String,
    val name: String,
    val subject: Subject,
    val signals: List<Signal>,
)

fun sentenceCase(id: String): String = id.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

class FingerprintBuilder {
    val signals = mutableListOf<Signal>()

    fun brand(vararg patterns: String) = patterns.mapTo(signals, Signal::Brand)

    fun channel(vararg patterns: String) = patterns.mapTo(signals, Signal::Channel)

    fun payload(vararg patterns: String) = patterns.mapTo(signals, Signal::Payload)

    fun viewDistanceAbove(limit: Int) = signals.add(Signal.ViewDistanceAbove(limit))

    fun keybind(vararg keys: String) = keys.mapTo(signals, Signal::Keybind)

    fun translation(vararg keys: String) = keys.mapTo(signals, Signal::Translation)
}

class CatalogBuilder {
    val fingerprints = mutableListOf<Fingerprint>()

    fun client(
        id: String,
        name: String = sentenceCase(id),
        block: FingerprintBuilder.() -> Unit,
    ) = add(id, name, Subject.CLIENT, block)

    fun loader(
        id: String,
        name: String = sentenceCase(id),
        block: FingerprintBuilder.() -> Unit,
    ) = add(id, name, Subject.LOADER, block)

    fun mod(
        id: String,
        name: String = sentenceCase(id),
        block: FingerprintBuilder.() -> Unit,
    ) = add(id, name, Subject.MOD, block)

    private fun add(
        id: String,
        name: String,
        subject: Subject,
        block: FingerprintBuilder.() -> Unit,
    ) {
        fingerprints += Fingerprint(id, name, subject, FingerprintBuilder().apply(block).signals)
    }
}

fun fingerprints(block: CatalogBuilder.() -> Unit): List<Fingerprint> = CatalogBuilder().apply(block).fingerprints
