package kr.hwaryuh.purity.nms

import io.papermc.paper.connection.PlayerConnection
import org.bukkit.entity.Player

// Records serverbound custom payload identifiers per connection. Implemented per server version, found via ServiceLoader.
interface PayloadTracker {
    // onNewInGame runs on the Netty thread.
    fun install(onNewInGame: (Player) -> Unit)

    fun uninstall()

    fun payloads(connection: PlayerConnection): Set<String>

    // Sends c:version and minecraft:register so Fabric and NeoForge clients disclose themselves during configuration.
    fun challenge(connection: PlayerConnection)

    fun payloads(player: Player): Set<String>
}
