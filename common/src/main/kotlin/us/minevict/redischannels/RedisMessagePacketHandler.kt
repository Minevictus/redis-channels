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
package us.minevict.redischannels

/**
 * A handler for a [RedisPacketChannel].
 *
 * @param P The type of packets to handle.
 */
interface RedisMessagePacketHandler<P> {
    /**
     * Handle a received packet.
     *
     * @param packet The packet received; may be null if [RedisPacketChannel.permitNulls] is true.
     * @param channel The channel name of the [RedisPacketChannel].
     */
    fun packetReceived(packet: P?, channel: String) = Unit

    /**
     * Handle a packet before it is sent.
     *
     * @param packet The packet to send; may be null if [RedisPacketChannel.permitNulls] is true.
     * @return The packet to send; may be null if [RedisPacketChannel.permitNulls] is true.
     */
    fun packetPreSend(packet: P?): P? = packet

    companion object {
        /**
         * Construct a new handler which only handles [packetPreSend]. This means [packetReceived] does nothing at all.
         *
         * @param block The handler for [packetPreSend].
         * @param P The type of packet to handle.
         * @return The packet handler.
         */
        fun <P> presend(block: (packet: P?) -> P?) =
            object : RedisMessagePacketHandler<P> {
                override fun packetPreSend(packet: P?): P? = block(packet)
            }

        /**
         * Construct a new handler which only handles [packetReceived]. This means [packetPreSend] is an identity method.
         *
         * @param block The handler for [packetReceived].
         * @param P The type of packet to handle.
         * @return The packet handler.
         */
        fun <P> recv(block: (packet: P?, channel: String) -> Unit) =
            object : RedisMessagePacketHandler<P> {
                override fun packetReceived(packet: P?, channel: String) = block(packet, channel)
            }

        /**
         * Constructs a new handler which does nothing out of the ordinary.
         *
         * @param P The type of packet to handle.
         * @return The packet handler.
         */
        fun <P> identity() = object : RedisMessagePacketHandler<P> {}
    }
}