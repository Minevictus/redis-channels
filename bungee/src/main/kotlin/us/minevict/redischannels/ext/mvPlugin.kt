package us.minevict.redischannels.ext

import us.minevict.mvutil.bungee.MvPlugin
import us.minevict.redischannels.RedisChannels
import us.minevict.redischannels.channels.RedisPacketChannel

fun MvPlugin.channels(vararg channels: RedisPacketChannel<*>) {
    channels.forEach {
        RedisChannels.instance.channel(it)
    }
}