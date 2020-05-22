package us.minevict.redischannels

import java.util.*

data class ServerGuidPacket(
    val guid: UUID
) {
    companion object {
        const val CHANNEL = "mvredischannels:srvguid"
    }
}