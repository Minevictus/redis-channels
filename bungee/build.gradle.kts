import us.minevict.mvutilgradleplugin.bungee
import us.minevict.mvutilgradleplugin.depend
import us.minevict.mvutilgradleplugin.mvutil
import us.minevict.mvutilgradleplugin.waterfallApi

dependencies {
    compileOnly("io.github.waterfallmc:waterfall-api:1.18-R0.1-SNAPSHOT")
    compileOnly(mvutil("bungee"))
    api(project(":common"))
}

bungee {
    name = "RedisChannels"
    main = "us.minevict.redischannels.RedisChannels"
    author = "Proximyst"
    depend()
}