package kr.hwaryuh.purity.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import kr.hwaryuh.purity.Inspector
import kr.hwaryuh.purity.fingerprint.FINGERPRINTS
import kr.hwaryuh.purity.info
import kr.hwaryuh.purity.probe.SignProbe
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PurityCommand(
    private val inspector: Inspector,
    private val probe: SignProbe,
    private val reload: () -> Unit,
) {
    fun build(): LiteralCommandNode<CommandSourceStack> =
        Commands
            .literal("purity")
            .requires { s -> listOf(INFO, PROBE, RELOAD).any { s.sender.hasPermission(it) } }
            .then(
                Commands
                    .literal("info")
                    .requires { it.sender.hasPermission(INFO) }
                    .then(
                        Commands.argument("player", ArgumentTypes.player()).executes { ctx ->
                            val player = player(ctx)
                            val o = inspector.observe(player)
                            val evidence = inspector.evidence(o)
                            val lines =
                                info(
                                    player.name,
                                    o,
                                    probe.results(player),
                                    evidence,
                                    inspector.verdict(evidence),
                                    inspector.settings.enforce,
                                )
                            ctx.source.sender.sendMessage(Component.text(lines.joinToString("\n")))
                            1
                        },
                    ),
            ).then(
                Commands
                    .literal("ids")
                    .requires { it.sender.hasPermission(INFO) }
                    .executes { ctx ->
                        val lines =
                            FINGERPRINTS.groupBy { it.subject }.map { (subject, fps) ->
                                "$subject: ${
                                    fps.joinToString(
                                        ", ",
                                    ) { if (it.id in inspector.settings.allow) "${it.id}(allowed)" else it.id }
                                }"
                            }
                        ctx.source.sender.sendMessage(Component.text(lines.joinToString("\n")))
                        1
                    },
            ).then(
                Commands
                    .literal("probe")
                    .requires { it.sender.hasPermission(PROBE) }
                    .then(
                        Commands.argument("player", ArgumentTypes.player()).executes { ctx ->
                            val player = player(ctx)
                            val message =
                                if (probe.start(player)) {
                                    "Probing ${player.name}. Results appear in /purity info."
                                } else {
                                    "A probe is already running for ${player.name}."
                                }
                            ctx.source.sender.sendMessage(Component.text(message))
                            1
                        },
                    ),
            ).then(
                Commands
                    .literal("reload")
                    .requires { it.sender.hasPermission(RELOAD) }
                    .executes { ctx ->
                        // Settings validation throws before the previous snapshot is replaced.
                        val message =
                            try {
                                reload()
                                "Purity reloaded."
                            } catch (e: IllegalArgumentException) {
                                "Reload failed, previous settings kept: ${e.message}"
                            }
                        ctx.source.sender.sendMessage(Component.text(message))
                        1
                    },
            ).build()

    private fun player(ctx: CommandContext<CommandSourceStack>): Player =
        ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).first()

    private companion object {
        const val INFO = "purity.info"
        const val PROBE = "purity.probe"
        const val RELOAD = "purity.reload"
    }
}
