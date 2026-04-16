plugins {
    id("xyz.jpenilla.run-paper")
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
}

version = "${rootProject.property("mod_version")}+${rootProject.property("minecraft_version")}"
group = rootProject.property("maven_group").toString()

base {
    archivesName = rootProject.property("archives_base_name") as String + "-paper"
}

repositories {
    maven("https://repo.nexomc.com/releases")
    maven("https://maven.devs.beer/")
    maven("https://repo.papermc.io/repository/maven-public/") // already in root, but can be per-module
}

dependencies {
    paperweight.paperDevBundle("${rootProject.property("minecraft_version")}.build.60-stable")
    compileOnly("io.papermc.paper:paper-api:${rootProject.property("minecraft_version")}.build.+")

    implementation(project(":common"))
    implementation(libs.bstats.bukkit)

    compileOnly("com.nexomc:nexo:1.23")
    compileOnly("dev.lone:api-itemsadder:4.0.10")
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        from(project(":common").sourceSets.main.get().output)
        relocate("org.bstats", project.group.toString())
    }

    jar {
        dependsOn(shadowJar)
        enabled = false
    }

    runServer {
        minecraftVersion(rootProject.property("minecraft_version") as String)
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    runPaper.folia.registerTask()

    processResources {
        filesMatching("plugin.yml") {
            expand(mapOf("version" to version))
        }
    }
}