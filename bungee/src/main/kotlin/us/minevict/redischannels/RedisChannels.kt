package us.minevict.redischannels

import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.Server
import us.minevict.mvutil.bungee.MvPlugin
import us.minevict.mvutil.bungee.channel.JsonPacketChannel
import us.minevict.mvutil.common.MvUtilVersion
import us.minevict.mvutil.common.channel.IPluginMessagePacketHandler
import us.minevict.redischannels.channels.RedisPacketChannel
import java.io.File
import java.util.*

class RedisChannels : MvPlugin() {
    private lateinit var serverManager: ServerManager

    private val redisChannels = mutableListOf<RedisPacketChannel<*>>()

    init {
        instance = this
    }

    override fun enable(): Boolean {
        MvUtilVersion.requireVersion(5, 1)
        dataFolder.mkdirs()
        serverManager = ServerManager(this, File(dataFolder, "servers.json"))

        packetChannel(JsonPacketChannel(
            this,
            ServerGuidPacket::class.java,
            ServerGuidPacket.CHANNEL,
            IPluginMessagePacketHandler.recv { packet, receiver, _ ->
                if (receiver is Server) serverManager.handleGuidPacket(packet ?: return@recv, receiver)
            }
        ))

        return true
    }

    override fun disable() {
        redisChannels.forEach(RedisPacketChannel<*>::unregister)
        redisChannels.clear()
    }

    fun channel(channel: RedisPacketChannel<*>) {
        redisChannels.add(channel)
    }

    fun getGuid(server: Server): UUID? = getGuid(server.info.name)
    fun getGuid(server: ServerInfo): UUID? = getGuid(server.name)
    fun getGuid(serverName: String): UUID? =
        serverManager.guidToNameMap.entries
            .firstOrNull { (_, v) -> v == serverName }
            ?.key

    fun getHash(server: Server): Int = getHash(server.info.name)
    fun getHash(server: ServerInfo): Int = getHash(server.name)
    fun getHash(serverName: String): Int =
        serverManager.hashToNameMap.entries
            .first { (_, v) -> v == serverName }
            .key

    fun getPubSubChannelName(server: Server): String? = getPubSubChannelName(server.info.name)
    fun getPubSubChannelName(server: ServerInfo): String? = getPubSubChannelName(server.name)
    fun getPubSubChannelName(serverName: String): String? {
        val guid = getGuid(serverName) ?: return null
        return "mvredischannels_server_$guid"
    }

    fun publishToServer(server: Server, subchannel: String, content: String): Boolean =
        publishToServer(server.info.name, subchannel, content)

    fun publishToServer(server: ServerInfo, subchannel: String, content: String): Boolean =
        publishToServer(server.name, subchannel, content)

    fun publishToServer(server: UUID, subchannel: String, content: String): Boolean {
        return publishToServer(serverManager.guidToNameMap[server] ?: return false, subchannel, content)
    }

    fun publishToServer(serverName: String, subchannel: String, content: String): Boolean {
        val pubsubName = getPubSubChannelName(serverName) ?: return false
        val payload = "$subchannel${0.toChar()}$content"
        mvUtil.redis.connectPubSub().use {
            it.sync().publish(pubsubName, payload)
        }
        return true
    }

    companion object {
        lateinit var instance: RedisChannels
            private set
    }
}