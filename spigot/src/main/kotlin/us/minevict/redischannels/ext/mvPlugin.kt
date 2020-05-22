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