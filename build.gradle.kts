
import nl.javadude.gradle.plugins.license.LicensePlugin
import us.minevict.mvutilgradleplugin.MvUtilPlugin
import us.minevict.mvutilgradleplugin.mvUtilVersion
import java.util.*

plugins {
    id("us.minevict.mvutil") version "0.2.5"
    id("com.github.hierynomus.license") version "0.15.0"
}

mvUtilVersion = "6.0.1"

allprojects {
    group = "us.minevict.redischannels"
    version = "0.3.1"
}

subprojects {
    apply {
        plugin<MvUtilPlugin>()
        plugin<LicensePlugin>()
    }

    license {
        header = rootProject.file("LICENCE-HEADER")
        ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
        ext["name"] = "Mariell Hoversholm"
        include("**/*.kt")
    }
}