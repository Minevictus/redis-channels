package us.minevict.redischannels.channels

import java.util.*

/**
 * A redis messaging channel which uses a specific packet type.
 *
 * @param P The type of packets to flow through this channel and its handler.
 */
interface RedisPacketChannel<P> {
    /**
     * Whether this channel permits nulls flowing through it.
     *
     * @return Whether this permits nulls.
     */
    val permitNulls: Boolean

    /**
     * Get the type of packet this channel handles.
     *
     * @return The type of packet this handles.
     */
    val packetType: Class<out P>

    /**
     * This channel's name.
     *
     * @return The name of this channel.
     */
    val channel: String

    /**
     * Send a packet through the channel.
     *
     * @param server Send the packet to this server.
     * @param packet The packet to send or null if [permitNulls] is true.
     * @return Whether the packet was successfully sent.
     */
    fun sendPacket(server: UUID?, packet: P?): Boolean

    /**
     * Unregister all that has to do with this channel from Redis.
     */
    fun unregister()
}