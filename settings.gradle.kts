dependencyResolutionManagement {
    repositories {
        maven("https://maven.nucleoid.xyz/releases")
    }
    versionCatalogs {
        create("polymerLibs") {
            from("eu.pb4:polymer-catalog:${providers.gradleProperty("polymer_version").get()}")
        }
    }
}
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "camera-obscura"

include("common", "paper", "fabric")