package kr.hwaryuh.purity.nms.v26_3

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.AttributeKey
import io.papermc.paper.connection.PaperCommonConnection
import io.papermc.paper.connection.PlayerConnection
import io.papermc.paper.network.ChannelInitializeListenerHolder
import kr.hwaryuh.purity.nms.PayloadTracker
import net.kyori.adventure.key.Key
import net.minecraft.network.Connection
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class PayloadTrackerImpl : PayloadTracker {
    override fun install(onNewInGame: (Player) -> Unit) {
        ChannelInitializeListenerHolder.addListener(KEY) { channel ->
            channel.attr(PAYLOADS).set(ConcurrentHashMap.newKeySet())
            channel.pipeline().addBefore("packet_handler", HANDLER, Handler(onNewInGame))
        }
    }

    override fun uninstall() {
        ChannelInitializeListenerHolder.removeListener(KEY)
        connections().forEach { it.channel.pipeline().runCatching { remove(HANDLER) } }
    }

    override fun payloads(connection: PlayerConnection): Set<String> = payloads(handle(connection).channel)

    // Paper's API only sends on channels the client registered, which nothing has yet.
    override fun challenge(connection: PlayerConnection) {
        val target = handle(connection)
        CHALLENGES.forEach { (id, body) -> target.send(ClientboundCustomPayloadPacket(DiscardedPayload(Identifier.parse(id), body))) }
    }

    private fun handle(connection: PlayerConnection): Connection =
        (PACKET_LISTENER.get(connection) as ServerCommonPacketListenerImpl).connection

    override fun payloads(player: Player): Set<String> =
        payloads(
            (player as CraftPlayer)
                .handle.connection.connection.channel,
        )

    private fun payloads(channel: Channel): Set<String> =
        channel
            .attr(PAYLOADS)
            .get()
            ?.toSet()
            .orEmpty()

    private fun connections(): List<Connection> {
        val list = MinecraftServer.getServer().connection.connections
        return synchronized(list) { list.toList() }
    }

    private class Handler(
        private val onNewInGame: (Player) -> Unit,
    ) : ChannelInboundHandlerAdapter() {
        override fun channelRead(
            ctx: ChannelHandlerContext,
            msg: Any,
        ) {
            if (msg is ServerboundCustomPayloadPacket) {
                val set = ctx.channel().attr(PAYLOADS).get()
                val id =
                    msg
                        .payload()
                        .type()
                        .id()
                        .toString()
                if (set != null && set.size < LIMIT && set.add(id)) {
                    val listener = (ctx.pipeline().get("packet_handler") as? Connection)?.packetListener
                    if (listener is ServerGamePacketListenerImpl) onNewInGame(listener.player.bukkitEntity)
                }
            }
            super.channelRead(ctx, msg)
        }
    }

    private companion object {
        const val HANDLER = "purity_payload"
        const val LIMIT = 256
        val KEY = Key.key("purity", "payload")
        val PAYLOADS: AttributeKey<MutableSet<String>> = AttributeKey.valueOf("purity_payloads")

        // Paper keeps the listener protected; reading it avoids scanning every server connection.
        val PACKET_LISTENER =
            PaperCommonConnection::class.java.getDeclaredField("packetListener").apply { isAccessible = true }

        // Both loaders answer c:version [1] (VarInt array) and disconnect on any other version; vanilla discards both.
        val CHALLENGES =
            listOf(
                "c:version" to byteArrayOf(1, 1),
                "minecraft:register" to "purity:probe".toByteArray(),
            )
    }
}
