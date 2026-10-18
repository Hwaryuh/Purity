package kr.hwaryuh.purity.guard

import kr.hwaryuh.poly.framework.di.Bean
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.Locale
import java.util.UUID

@Bean
class GuardFilter(
    private val rules: GuardRules,
) {
    fun blocks(
        player: UUID,
        command: String,
        exists: (String) -> Boolean,
    ): Boolean {
        val snapshot = rules.snapshot
        val label = command.trimStart().takeWhile { !it.isWhitespace() }
        return player !in snapshot.bypassPlayers && GuardPolicy.isBlocked(label, snapshot.blockedCommands, exists)
    }

    fun hidden(
        player: UUID,
        names: Set<String>,
    ): List<String> {
        val snapshot = rules.snapshot
        if (player in snapshot.bypassPlayers) return emptyList()
        val normalized = names.map { it.lowercase(Locale.ROOT) }.toSet()
        return names.filter { GuardPolicy.isBlocked(it, snapshot.blockedCommands, normalized::contains) }
    }

    fun rejection(command: String): List<Component> {
        val error = Component.text(command, NamedTextColor.RED).decoration(TextDecoration.UNDERLINED, true)
        val here =
            Component
                .translatable("command.context.here", NamedTextColor.RED)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.ITALIC, true)
        return listOf(
            Component.translatable("command.unknown.command", NamedTextColor.RED),
            error.append(here).clickEvent(ClickEvent.suggestCommand("/$command")),
        )
    }
}
