package us.minevict.redischannels

import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import us.minevict.mvutil.spigot.channel.PacketChannel
import us.minevict.mvutil.spigot.ext.skedule
import java.util.concurrent.atomic.AtomicBoolean

internal class ServerGuidSender(
    private val main: RedisChannels,
    private val channel: PacketChannel<ServerGuidPacket>
) : Listener {
    private val hasSent = AtomicBoolean(false)

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        val player = event.player
        main.skedule(false) {
            waitFor(5)
            if (!player.isOnline) return@skedule
            if (!hasSent.compareAndSet(false, true)) return@skedule // Already been sent!
            if (!channel.sendPacket(player, ServerGuidPacket(main.serverGuid))) {
                main.logger.severe("Could not send GUID!")
                hasSent.set(false)
                return@skedule
            }
            HandlerList.unregisterAll(this@ServerGuidSender)
            main.logger.info("Sent server GUID to BungeeCord!")
            channel.unregisterIncoming()
            main.server.messenger.unregisterOutgoingPluginChannel(main, channel.channel)
        }
    }
}