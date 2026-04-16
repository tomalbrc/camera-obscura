plugins {
    id("net.fabricmc.fabric-loom")
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
}
