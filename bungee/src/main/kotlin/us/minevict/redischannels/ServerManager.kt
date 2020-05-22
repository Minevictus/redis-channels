package us.minevict.redischannels

import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.plugin.Listener
import us.minevict.mvutil.common.ext.fromJson
import us.minevict.mvutil.common.ext.simpleGson
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Manager for all servers available.
 */
internal class ServerManager(
    private val main: RedisChannels,
    private val dataFile: File
) : Listener {
    private val writeLock = ReentrantLock()

    /**
     * Map with relations of GUIDs for servers to their respective names on the BungeeCord proxy.
     */
    val guidToNameMap = mutableMapOf<UUID, String>()

    /**
     * Map with relations of the hashes of the server info (name + socket address) to their respective names on the
     * BungeeCord proxy.
     */
    val hashToNameMap = mutableMapOf<Int, String>()

    init {
        dataFile.parentFile.mkdirs()
        val dataMap: Map<Int, UUID>? = if (!dataFile.isFile) null
        else FileReader(dataFile).use {
            @Suppress("UnstableApiUsage")
            simpleGson.fromJson<HashMap<Int, UUID>>(it)
        }

        for ((name, server) in main.proxy.serversCopy) {
            val hash = Objects.hash(server.name, server.socketAddress)
            hashToNameMap[hash] = name
            val guid = dataMap?.get(hash)
            guidToNameMap[guid ?: continue] = name
        }

        write()
    }

    /**
     * Handle setting a new GUID and saving the data of the current servers.
     */
    fun handleGuidPacket(packet: ServerGuidPacket, server: Server) {
        main.logger.info(
            "Associating ${server.info.name} (${Objects.hash(
                server.info.name,
                server.info.socketAddress
            )}) with GUID ${packet.guid}"
        )
        guidToNameMap[packet.guid] = server.info.name
        main.proxy.scheduler.runAsync(main) {
            try {
                write() // Blocking due to lock
            } catch (ex: IOException) {
                main.logger.warning("Could not save data about servers!")
                ex.printStackTrace()
            }
        }
    }

    /**
     * Write the current server data to file.
     *
     * This stores the server hash (calculated at runtime) to GUID (shared, saved data) such that we can identify which
     * server is which.
     */
    @Throws(IOException::class)
    fun write() {
        writeLock.withLock {
            val dataMap = mutableMapOf<Int, UUID>()
            for ((name, server) in main.proxy.serversCopy) {
                val hash = Objects.hash(server.name, server.socketAddress)
                val guid = guidToNameMap.entries.firstOrNull { (_, n) -> name == n }?.key
                dataMap[hash] = guid ?: continue
            }
            dataFile.writeText(simpleGson.toJson(dataMap))
        }
    }
}