package kr.hwaryuh.purity.config

import kr.hwaryuh.poly.config.ConfigDocuments
import kr.hwaryuh.purity.fingerprint.Subject
import java.io.File

class SettingsFile(
    private val documents: ConfigDocuments,
    private val file: File,
    bundled: String,
) {
    private val defaults = PurityOptionsLoader.loadFromString(documents, bundled)

    fun load(): Settings = loadSnapshot().settings

    fun loadSnapshot(): LoadedSettings {
        val options = PurityOptionsLoader.load(documents.read(file))
        val kickUnknown =
            when (options.unknown) {
                "kick" -> true
                "log" -> false
                else -> throw IllegalArgumentException("unknown must be kick or log, got ${options.unknown}")
            }
        val settings =
            Settings(
                enforce = options.enforce,
                allow = options.allow,
                kickUnknown = kickUnknown,
                bypass = options.bypass,
                probesEnabled = options.probes.enabled,
                probeTimeoutTicks = options.probes.timeoutTicks,
                messages = options.messages.toMessages(defaults.messages),
                logDetections = options.logging.detections,
                logChannels = options.logging.channels,
            )
        return LoadedSettings(settings, options.detection.enabled)
    }
}

data class LoadedSettings(
    val settings: Settings,
    val detectionEnabled: Boolean,
)

private fun MessageOptions.toMessages(defaults: MessageOptions): Messages {
    val translations =
        mapOf(
            Subject.CLIENT to (defaults.blockedClient to blockedClient),
            Subject.LOADER to (defaults.blockedLoader to blockedLoader),
            Subject.MOD to (defaults.blockedMod to blockedMod),
            Subject.UNKNOWN to (defaults.blockedUnknown to blockedUnknown),
        )
    val byLocale = linkedMapOf<String, MutableMap<Subject, String>>()
    for ((subject, maps) in translations) {
        val merged = maps.first.mapKeys { it.key.lowercase() } + maps.second.mapKeys { it.key.lowercase() }
        for ((locale, text) in merged) {
            byLocale.getOrPut(locale) { linkedMapOf() }[subject] = text
        }
    }
    return Messages(defaultLocale.lowercase(), byLocale)
}
