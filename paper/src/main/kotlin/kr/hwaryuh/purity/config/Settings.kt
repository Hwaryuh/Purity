package kr.hwaryuh.purity.config

import kr.hwaryuh.purity.fingerprint.FINGERPRINTS
import org.bukkit.configuration.ConfigurationSection
import java.util.UUID

class Settings(
    val enforce: Boolean,
    val allow: Set<String>,
    val kickUnknown: Boolean,
    val bypass: Set<UUID>,
    val probesEnabled: Boolean,
    val probeTimeoutTicks: Long,
    val messages: Messages,
    val logDetections: Boolean,
    val logChannels: Boolean,
) {
    init {
        // A typo here would kick players the operator meant to allow.
        val unknown = allow - FINGERPRINTS.map { it.id }.toSet()
        require(unknown.isEmpty()) { "Unknown ids in allow: $unknown" }
    }

    companion object {
        fun from(c: ConfigurationSection): Settings =
            Settings(
                enforce = c.getBoolean("enforce", true),
                allow = c.getStringList("allow").toSet(),
                kickUnknown =
                    when (val mode = c.getString("unknown", "kick")) {
                        "kick" -> true
                        "log" -> false
                        else -> throw IllegalArgumentException("unknown must be kick or log, got $mode")
                    },
                bypass = c.getStringList("bypass").map(UUID::fromString).toSet(),
                probesEnabled = c.getBoolean("probes.enabled"),
                probeTimeoutTicks = c.getLong("probes.timeout-ticks", 40),
                messages = Messages.from(c.getConfigurationSection("messages")),
                logDetections = c.getBoolean("logging.detections"),
                logChannels = c.getBoolean("logging.channels"),
            )
    }
}
