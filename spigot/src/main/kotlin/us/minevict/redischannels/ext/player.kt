package us.minevict.redischannels.ext

import org.bukkit.OfflinePlayer
import us.minevict.mvutil.spigot.MinevictusUtilsSpigot

val OfflinePlayer.isOnlineNetwork: Boolean
    get() = MinevictusUtilsSpigot.instance.redis.resource.use {
        it.sismember("network_players", uniqueId.toString())
    }