import us.minevict.mvutilgradleplugin.bukkit
import us.minevict.mvutilgradleplugin.depend
import us.minevict.mvutilgradleplugin.mvutil
import us.minevict.mvutilgradleplugin.paperApi

dependencies {
    compileOnly(paperApi("1.16.5"))
    compileOnly(mvutil("spigot"))
    api(project(":common"))
}

bukkit {
    name = "RedisChannels"
    main = "us.minevict.redischannels.RedisChannels"
    authors = listOf("Proximyst")
    depend()
}