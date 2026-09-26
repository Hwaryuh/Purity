package kr.hwaryuh.purity.nms

import io.papermc.paper.connection.PlayerConnection
import org.bukkit.entity.Player

// Records serverbound custom payload identifiers per connection. Implemented per server version, found via ServiceLoader.
interface PayloadTracker {
    // onNewInGame runs on the Netty thread.
    fun install(onNewInGame: (Player) -> Unit)

    fun uninstall()

    fun payloads(connection: PlayerConnection): Set<String>

    fun payloads(player: Player): Set<String>
}
