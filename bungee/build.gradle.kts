import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

plugins {
    id("net.minecrell.plugin-yml.bungee") version "0.3.0"
}

dependencies {
    compileOnly("io.github.waterfallmc:waterfall-api:1.15-SNAPSHOT")
    compileOnly("us.minevict.mvutil:bungee:${rootProject.ext["mvutilVer"]}")
    implementation(project(":common"))
}

tasks.withType<ShadowJar> {
    val body = HttpClient.newHttpClient().send(
        HttpRequest.newBuilder()
            .uri(uri("https://raw.githubusercontent.com/Minevictus/MV-Util/relocations/relocations.bungee.json"))
            .build(),
        HttpResponse.BodyHandlers.ofString()
    ).body()
    val json = groovy.json.JsonSlurper().parseText(body) as Map<String, String>
    json.forEach { (k, v) -> relocate(k, v) }
}

bungee {
    name = "RedisChannels"
    main = "us.minevict.redischannels.RedisChannels"
    author = "Proximyst"
    depends = setOf("MV-Util")
}