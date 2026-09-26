package kr.hwaryuh.purity.fingerprint

class Fingerprint(
    val id: String,
    val subject: Subject,
    val signals: List<Signal>,
)

class FingerprintBuilder {
    val signals = mutableListOf<Signal>()

    fun brand(vararg patterns: String) = patterns.mapTo(signals, Signal::Brand)

    fun channel(vararg patterns: String) = patterns.mapTo(signals, Signal::Channel)

    fun payload(vararg patterns: String) = patterns.mapTo(signals, Signal::Payload)

    fun keybind(vararg keys: String) = keys.mapTo(signals, Signal::Keybind)

    fun translation(vararg keys: String) = keys.mapTo(signals, Signal::Translation)
}

class CatalogBuilder {
    val fingerprints = mutableListOf<Fingerprint>()

    fun client(
        id: String,
        block: FingerprintBuilder.() -> Unit,
    ) = add(id, Subject.CLIENT, block)

    fun loader(
        id: String,
        block: FingerprintBuilder.() -> Unit,
    ) = add(id, Subject.LOADER, block)

    fun mod(
        id: String,
        block: FingerprintBuilder.() -> Unit,
    ) = add(id, Subject.MOD, block)

    private fun add(
        id: String,
        subject: Subject,
        block: FingerprintBuilder.() -> Unit,
    ) {
        fingerprints += Fingerprint(id, subject, FingerprintBuilder().apply(block).signals)
    }
}

fun fingerprints(block: CatalogBuilder.() -> Unit): List<Fingerprint> = CatalogBuilder().apply(block).fingerprints
