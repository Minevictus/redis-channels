import us.minevict.mvutilgradleplugin.bukkit
import us.minevict.mvutilgradleplugin.depend
import us.minevict.mvutilgradleplugin.mvutil

repositories {
    maven {
        name = "papermc-repo"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly(mvutil("spigot"))
    api(project(":common"))
}

bukkit {
    name = "RedisChannels"
    main = "us.minevict.redischannels.RedisChannels"
    authors = listOf("Proximyst")
    depend()
}