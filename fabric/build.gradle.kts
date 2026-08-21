plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
    alias(libs.plugins.shadow)
}

version = "${project.property("mod_version")}+${rootProject.property("minecraft_version")}"
group = rootProject.property("maven_group")!!

base {
    archivesName = rootProject.property("archives_base_name") as String + "-fabric"
}

repositories {
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    implementation(polymerLibs.polymer.resourcepack)
    implementation(polymerLibs.polymer.resourcepack.extras)
    implementation(polymerLibs.polymer.core)
    implementation(polymerLibs.polymer.virtualentity)
    implementation(polymerLibs.polymer.autohost)

    shadow(implementation(project(":common"))!!)
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", rootProject.property("minecraft_version"))
    inputs.property("loader_version", libs.versions.loader.get())

    filesMatching("fabric.mod.json") {
        expand(
            "version" to inputs.properties.get("version")!!,
            "minecraft_version" to inputs.properties.get("minecraft_version")!!,
            "loader_version" to inputs.properties.get("loader_version")!!
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("")

    isZip64 = true

    relocate("me.lucko.fabric.api", "de.tomalbrc.cameraobscura.shade.permissionapi")

    exclude("META-INF/*.SF", "ETA-INF/*.DSA", "META-INF/*.RSA")
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar)
            groupId = project.group.toString()
            artifactId = base.archivesName.get()
            version = project.version.toString()
        }
    }
    repositories {

    }
}