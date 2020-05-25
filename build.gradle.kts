import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.3.71"
    id("com.github.hierynomus.license") version "0.15.0"
}

ext["mvutilVer"] = "5.1.0"

run {
    val props = Properties()
    rootDir.listFiles { file -> file.extension == "properties" && file.nameWithoutExtension != "gradle" }
        ?.forEach {
            println("Loading ${it.name}...")
            it.inputStream().use {
                props.load(it)
            }
        }
    props.forEach {
        project.ext[it.key.toString()] = it.value
    }
}

allprojects {
    group = "us.minevict.redischannels"
    version = "0.1.0"
}

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<ShadowPlugin>()
        plugin<MavenPublishPlugin>()
        plugin("org.jetbrains.kotlin.jvm")
        plugin<LicensePlugin>()
    }

    repositories {
        maven {
            name = "spigotmc"
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

            content {
                includeGroup("org.bukkit")
                includeGroup("org.spigotmc")
            }
        }

        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")

            content {
                includeGroup("net.md-5")
            }
        }

        maven {
            name = "papermc-snapshots"
            url = uri("https://papermc.io/repo/repository/maven-snapshots/")

            content {
                includeGroup("com.destroystokyo.paper")
                includeGroup("io.github.waterfallmc")
                includeGroup("io.papermc")
            }
        }

        maven {
            name = "papermc"
            url = uri("https://papermc.io/repo/repository/maven-public/")

            content {
                includeGroup("com.destroystokyo.paper")
                includeGroup("io.github.waterfallmc")
                includeGroup("io.papermc")
            }
        }

        maven {
            name = "aikar-repo"
            url = uri("https://repo.aikar.co/content/groups/aikar/")

            content {
                includeGroup("co.aikar")
            }
        }

        maven {
            name = "minebench"
            url = uri("https://repo.minebench.de/")

            content {
                includeGroup("de.themoep")
            }
        }

        maven {
            name = "okkero"
            url = uri("https://nexus.okkero.com/repository/maven-releases")

            content {
                includeGroup("com.okkero.skedule")
            }
        }

        maven {
            name = "proxi-nexus"
            url = uri("https://nexus.proximyst.com/repository/maven-any/")
        }

        maven {
            name = "bintray-chatmenuapi"
            url = uri("https://dl.bintray.com/nahuld/minevictus/")
        }

        jcenter()
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            javaParameters = true
        }
    }

    tasks.withType<JavaCompile> {
        options.isFork = true
        options.compilerArgs.add("-parameters")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = sourceCompatibility
    }

    tasks.withType<ShadowJar> {
        archiveBaseName.set("${rootProject.name}-${project.name}")
        destinationDirectory.set(rootProject.tasks.shadowJar.get().destinationDirectory.get())
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(sourcesJar.get())
            }
        }
        repositories {
            maven {
                name = "proxi-nexus"
                url = uri("https://nexus.proximyst.com/repository/maven-any/")
                credentials {
                    val proxiUser: String? by rootProject
                    val proxiPassword: String? by rootProject
                    username = proxiUser
                    password = proxiPassword
                }
            }
        }
    }

    license {
        header = rootProject.file("LICENCE-HEADER")
        ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
        ext["name"] = "Mariell Hoversholm"
        include("**/*.kt")
    }
}

// Holy SHIT IntelliJ, I do not care about some agreement for Java 14, we're not using it!
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
}