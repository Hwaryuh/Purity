package kr.hwaryuh.purity.command

import kr.hwaryuh.poly.command.Arg
import kr.hwaryuh.poly.command.Child
import kr.hwaryuh.poly.command.Command
import kr.hwaryuh.poly.command.Permission
import kr.hwaryuh.poly.command.Sender
import kr.hwaryuh.poly.framework.di.Reloader
import kr.hwaryuh.purity.Purity
import kr.hwaryuh.purity.fingerprint.FINGERPRINTS
import kr.hwaryuh.purity.info
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

private const val INFO = "purity.info"
private const val PROBE = "purity.probe"
private const val RELOAD = "purity.reload"

@Command(label = "purity", help = false)
@Permission(INFO, PROBE, RELOAD)
class PurityCommand(
    private val purity: Purity,
    private val reloader: Reloader,
) {
    @Child("info")
    @Permission(INFO)
    fun info(
        @Sender sender: CommandSender,
        @Arg("player") player: Player,
    ): Component {
        if (!purity.detectionEnabled) {
            return Component.text(if (sender is Player) "감지 기능이 비활성화되어 있습니다." else "Detection is disabled.")
        }
        val inspector = purity.inspector
        val observation = inspector.observe(player)
        val evidence = inspector.evidence(observation)
        val lines =
            info(
                player.name,
                observation,
                purity.probe.results(player),
                evidence,
                inspector.verdict(evidence),
                inspector.settings.enforce,
            )
        return Component.text(lines.joinToString("\n"))
    }

    @Child("ids")
    @Permission(INFO)
    fun ids(): Component {
        val lines =
            FINGERPRINTS.groupBy { it.subject }.map { (subject, fingerprints) ->
                "$subject: ${fingerprints.joinToString(", ") { if (it.id in purity.settings.allow) "${it.id}(allowed)" else it.id }}"
            }
        return Component.text(lines.joinToString("\n"))
    }

    @Child("probe")
    @Permission(PROBE)
    fun probe(
        @Sender sender: CommandSender,
        @Arg("player") player: Player,
    ): Component {
        if (!purity.detectionEnabled) {
            return Component.text(if (sender is Player) "감지 기능이 비활성화되어 있습니다." else "Detection is disabled.")
        }
        val started = purity.probe.start(player)
        val message =
            when {
                sender is Player && started -> "${player.name} 님의 검사를 시작했습니다. 결과는 /purity info에서 확인할 수 있습니다."
                sender is Player -> "${player.name} 님의 검사가 이미 진행 중입니다."
                started -> "Probing ${player.name}; see /purity info."
                else -> "A probe is already running for ${player.name}."
            }
        return Component.text(message)
    }

    @Child("reload")
    @Permission(RELOAD)
    fun reload(
        @Sender sender: CommandSender,
    ): Component {
        val result = reloader.reload(Purity.RELOAD_ID)
        val success = Purity.RELOAD_ID in result.reloaded
        val message =
            when {
                sender is Player && success -> "Purity 설정을 다시 불러왔습니다."
                sender is Player -> "설정을 불러오지 못해 이전 설정을 유지합니다."
                success -> "Purity reloaded."
                else -> "Reload failed; previous settings kept."
            }
        return Component.text(message)
    }
}
