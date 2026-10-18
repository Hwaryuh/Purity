package kr.hwaryuh.purity.guard

import kr.hwaryuh.poly.config.Comment
import kr.hwaryuh.poly.config.Config
import kr.hwaryuh.poly.config.SerialName
import java.util.Locale
import java.util.UUID

@Config(hasDefault = true)
data class GuardOptions(
    @Comment("UUIDs allowed to bypass command blocking and use Poly restricted commands.")
    @SerialName("bypass-players")
    val bypassPlayers: Set<UUID> = emptySet(),
    @Comment("Command names or namespaces ending in ':', e.g. [plugins, 'bukkit:'].")
    @SerialName("blocked-commands")
    val blockedCommands: Set<String> = emptySet(),
)

class GuardSnapshot private constructor(
    val bypassPlayers: Set<UUID>,
    val blockedCommands: Set<String>,
) {
    companion object {
        fun of(options: GuardOptions): GuardSnapshot =
            GuardSnapshot(
                options.bypassPlayers.toSet(),
                options.blockedCommands.map { it.trim().lowercase(Locale.ROOT) }.toSet(),
            )
    }
}

interface GuardRules {
    val snapshot: GuardSnapshot
}
