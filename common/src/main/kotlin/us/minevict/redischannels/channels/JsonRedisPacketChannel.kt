/**
 * MV-Util
 * Copyright (C) 2020 Mariell Hoversholm
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package us.minevict.redischannels.channels

import com.google.gson.JsonSyntaxException
import redis.clients.jedis.JedisPubSub
import redis.clients.jedis.exceptions.JedisConnectionException
import us.minevict.mvutil.common.IMvPlugin
import us.minevict.mvutil.common.ext.simpleGson
import us.minevict.redischannels.PROXY_REDIS_NAME
import us.minevict.redischannels.RedisMessagePacketHandler
import java.net.SocketException
import java.util.UUID
import kotlin.concurrent.thread

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
) : JedisPubSub(), RedisPacketChannel<P> {
    private val fullyQualifiedChannelName = "mvredischannels_${serverGuid ?: PROXY_REDIS_NAME}_$channel"
    private val redisPubSubThread = thread(start = true, isDaemon = true) {
        while (true) {
            try {
                plugin.mvUtil.redis.resource.use {
                    it.subscribe(this, fullyQualifiedChannelName)
                }
            } catch (ex: JedisConnectionException) {
                if (ex.cause !is SocketException) // Only re-throw if the connection died
                    throw ex
                break
            } catch (ignored: InterruptedException) {
                break
            }
        }
    }

    override fun sendPacket(serverGuid: UUID?, packet: P?): Boolean {
        if (!permitNulls && packet == null) throw IllegalArgumentException("does not permit nulls but attempted null packet")

        val handledPacket = handler.packetPreSend(packet)
        if (!permitNulls && handledPacket == null) throw IllegalArgumentException("does not permit nulls but attempted null packet")

        val jsonMessage = if (handledPacket == null) ""
        else simpleGson.toJson(handledPacket)

        plugin.mvUtil.redis.resource.use {
            it.publish("mvredischannels_${serverGuid ?: PROXY_REDIS_NAME}_$channel", jsonMessage)
        }
        return true
    }

    override fun onMessage(channel: String, message: String) {
        runCatching {
            if (channel != this.fullyQualifiedChannelName) return // Incorrect channel found.

            if (message.isEmpty()) {
                // No data, null packet received!
                if (!permitNulls) throw IllegalArgumentException("does not permit nulls but attempted null packet")
                handler.packetReceived(null, channel)
                return
            }

            @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") // It's necessary due to the catch arm
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
            .onFailure {
                Exception("error upon reading from redischannel $channel", it).printStackTrace()
            }
    }

    override fun unregister() {
        redisPubSubThread.interrupt()
    }
}