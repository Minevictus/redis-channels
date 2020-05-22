import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.3.0"
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
    compileOnly("us.minevict.mvutil:spigot:${rootProject.ext["mvutilVer"]}")
    implementation(project(":common"))
}

tasks.withType<ShadowJar> {
    val body = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder()
            .uri(uri("https://raw.githubusercontent.com/Minevictus/MV-Util/relocations/relocations.spigot.json"))
            .build(),
        HttpResponse.BodyHandlers.ofString()
    ).body()
    val json = groovy.json.JsonSlurper().parseText(body) as Map<String, String>
    json.forEach { (k, v) -> relocate(k, v) }
}

bukkit {
    name = "RedisChannels"
    main = "us.minevict.redischannels.RedisChannels"
    authors = listOf("Proximyst")
    depend = listOf("MV-Util")
}