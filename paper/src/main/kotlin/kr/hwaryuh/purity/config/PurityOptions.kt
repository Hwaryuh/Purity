package kr.hwaryuh.purity.config

import kr.hwaryuh.poly.config.Config
import kr.hwaryuh.poly.config.SerialName
import java.util.UUID

@Config(hasDefault = true)
data class PurityOptions(
    val enforce: Boolean = true,
    val allow: Set<String> = emptySet(),
    val unknown: String = "kick",
    val bypass: Set<UUID> = emptySet(),
    val probes: ProbeOptions = ProbeOptions(),
    val logging: LoggingOptions = LoggingOptions(),
    val messages: MessageOptions = MessageOptions(),
)

@Config(hasDefault = true)
data class ProbeOptions(
    val enabled: Boolean = true,
    @SerialName("timeout-ticks") val timeoutTicks: Long = 40,
)

@Config(hasDefault = true)
data class LoggingOptions(
    val detections: Boolean = true,
    val channels: Boolean = false,
)

@Config(hasDefault = true)
data class MessageOptions(
    @SerialName("default") val defaultLocale: String = "ko_kr",
    @SerialName("blocked-client") val blockedClient: Map<String, String> = emptyMap(),
    @SerialName("blocked-loader") val blockedLoader: Map<String, String> = emptyMap(),
    @SerialName("blocked-mod") val blockedMod: Map<String, String> = emptyMap(),
    @SerialName("blocked-unknown") val blockedUnknown: Map<String, String> = emptyMap(),
)
