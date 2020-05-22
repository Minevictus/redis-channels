package us.minevict.redischannels.channels

import com.google.gson.JsonSyntaxException
import us.minevict.mvutil.common.IMvPlugin
import us.minevict.mvutil.common.ext.simpleGson
import us.minevict.mvutil.common.redis.DefaultRedisPubSubListener
import us.minevict.redischannels.PROXY_REDIS_NAME
import us.minevict.redischannels.RedisMessagePacketHandler
import java.util.*

/**
 * A [RedisPacketChannel] which transmits its packets through serializing as JSON.
 *
 * @param P The type of packets to flow through this channel and its handler.
 */
class JsonRedisPacketChannel<P>(
    private val plugin: IMvPlugin<*, *, *>,
    /**
     * The GUID of the server for this channel.
     *
     * If this is `null`, it is the proxy name.
     */
    serverGuid: UUID?,
    override val channel: String,
    override val packetType: Class<out P>,
    private val handler: RedisMessagePacketHandler<P> = RedisMessagePacketHandler.identity(),
    override val permitNulls: Boolean = false
) : RedisPacketChannel<P>, DefaultRedisPubSubListener<String, String> {
    private val fullyQualifiedChannelName = "mvredischannels_${serverGuid ?: PROXY_REDIS_NAME}_$channel"
    private val redisPubSub = plugin.mvUtil.redis.connectPubSub().also {
        it.addListener(this)
        it.async().subscribe(fullyQualifiedChannelName)
    }

    override fun sendPacket(serverGuid: UUID?, packet: P?): Boolean {
        if (!permitNulls && packet == null) throw IllegalArgumentException("does not permit nulls but attempted null packet")

        val handledPacket = handler.packetPreSend(packet)
        if (!permitNulls && handledPacket == null) throw IllegalArgumentException("does not permit nulls but attempted null packet")

        val jsonMessage = if (handledPacket == null) ""
        else simpleGson.toJson(handledPacket)

        plugin.mvUtil.redis.connect().use {
            it.sync().publish("mvredischannels_${serverGuid ?: PROXY_REDIS_NAME}_$channel", jsonMessage)
        }
        return true
    }

    override fun message(channel: String, message: String) {
        if (channel != this.fullyQualifiedChannelName) return // Incorrect channel found.

        if (message.isEmpty()) {
            // No data, null packet received!
            if (!permitNulls) throw IllegalArgumentException("does not permit nulls but attempted null packet")
            handler.packetReceived(null, channel)
            return
        }

        var packet: P? = null
        try {
            packet = simpleGson.fromJson(message, packetType)
        } catch (ex: JsonSyntaxException) {
            plugin.platformLogger.warning("Received malformed packet on plugin messaging channel: $channel")
            plugin.platformLogger.warning("Received packet: $packet")
            ex.printStackTrace()
            return
        }

        // Packet is not null by now.
        handler.packetReceived(packet, channel)
    }

    override fun unregister() {
        redisPubSub.close()
    }
}