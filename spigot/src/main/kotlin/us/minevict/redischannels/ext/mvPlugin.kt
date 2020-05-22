package us.minevict.redischannels.ext

import org.bukkit.plugin.java.JavaPlugin
import us.minevict.mvutil.spigot.MvPlugin
import us.minevict.redischannels.RedisChannels
import us.minevict.redischannels.channels.RedisPacketChannel

fun MvPlugin.channels(vararg channels: RedisPacketChannel<*>) {
    val pl = JavaPlugin.getPlugin(RedisChannels::class.java)
    channels.forEach {
        pl.channel(this, it)
    }
}