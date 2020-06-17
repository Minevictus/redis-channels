
import nl.javadude.gradle.plugins.license.LicensePlugin
import us.minevict.mvutilgradleplugin.MvUtilPlugin
import us.minevict.mvutilgradleplugin.mvUtilVersion
import java.util.*

plugins {
    id("us.minevict.mvutil") version "0.2.4"
    id("com.github.hierynomus.license") version "0.15.0"
}

mvUtilVersion = "6.0.0"
ext["mvutilVer"] = "6.0.0"

allprojects {
    group = "us.minevict.redischannels"
    version = "0.2.0"
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