package us.minevict.redischannels.ext

import net.md_5.bungee.api.connection.ProxiedPlayer
import us.minevict.mvutil.bungee.MinevictusUtilsBungee

val ProxiedPlayer.isOnlineNetwork: Boolean
    get() = isConnected || MinevictusUtilsBungee.instance.redis.resource.use {
        it.sismember("network_players", uniqueId.toString())
    }