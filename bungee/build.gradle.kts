import us.minevict.mvutilgradleplugin.bungee
import us.minevict.mvutilgradleplugin.depend
import us.minevict.mvutilgradleplugin.mvutil
import us.minevict.mvutilgradleplugin.waterfallApi

dependencies {
    compileOnly(waterfallApi("1.15"))
    compileOnly(mvutil("bungee"))
    api(project(":common"))
}

bungee {
    name = "RedisChannels"
    main = "us.minevict.redischannels.RedisChannels"
    author = "Proximyst"
    depend()
}