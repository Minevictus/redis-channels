package us.minevict.redischannels

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import us.minevict.mvutil.common.MvUtilVersion
import us.minevict.mvutil.common.channel.IPluginMessagePacketHandler
import us.minevict.mvutil.spigot.MvPlugin
import us.minevict.mvutil.spigot.channel.JsonPacketChannel
import us.minevict.redischannels.channels.RedisPacketChannel
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class RedisChannels : MvPlugin(), Listener {
    lateinit var serverGuid: UUID
        private set

    private val redisChannels = mutableMapOf<Plugin, MutableList<RedisPacketChannel<*>>>()

    override fun enable(): Boolean {
        MvUtilVersion.requireVersion(5, 1)
        dataFolder.mkdirs()
        var hasSetGuid = false
        val guidFile = File(dataFolder, "guid.bin") // Stores the GUID for this server
        if (guidFile.isFile) {
            // Exists.. or so we hope.
            try {
                val buf = ByteBuffer.wrap(guidFile.readBytes()).asLongBuffer()
                val leastSignificant = buf.get()
                val mostSignificant = buf.get()
                serverGuid = UUID(mostSignificant, leastSignificant)
                hasSetGuid = true
            } catch (ignored: Exception) {
            }
        }
        if (!hasSetGuid) {
            // It did not exist or something went wrong.
            // Save a new one.
            serverGuid = UUID.randomUUID()
            val buf = ByteBuffer.allocate(Long.SIZE_BYTES * 2)
            buf.putLong(serverGuid.leastSignificantBits)
            buf.putLong(serverGuid.mostSignificantBits)
            guidFile.writeBytes(buf.array())
        }
        logger.info("Our identity is: $serverGuid")
        // Always send GUID, just to be sure.
        val guidChannel = packetChannel(
            JsonPacketChannel(
                this,
                ServerGuidPacket::class.java,
                ServerGuidPacket.CHANNEL,
                IPluginMessagePacketHandler.recv { _, _, _ -> }
            )
        )
        listeners(ServerGuidSender(this, guidChannel))
        // We now have a server GUID the bungeecord should recognise.

        listeners(this)
        return true
    }

    override fun disable() {
        for ((_, channels) in redisChannels)
            channels.forEach(RedisPacketChannel<*>::unregister)
        redisChannels.clear()
    }

    fun channel(plugin: Plugin, channel: RedisPacketChannel<*>) {
        this.redisChannels.compute(plugin) { _, value ->
            val list = value ?: mutableListOf()
            list.add(channel)
            list
        }
    }

    @EventHandler
    fun disablePlugin(event: PluginDisableEvent) {
        redisChannels[event.plugin]?.forEach(RedisPacketChannel<*>::unregister)
    }
}