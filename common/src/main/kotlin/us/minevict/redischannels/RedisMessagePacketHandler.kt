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