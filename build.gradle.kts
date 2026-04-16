import proguard.gradle.ProGuardTask

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.8.2")
    }
}

plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    id("xyz.jpenilla.run-paper") version "3.0.2" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta+" apply false
    id("com.gradleup.shadow") version "9.4.1" apply false
}

version = "${rootProject.property("mod_version")}+${rootProject.property("minecraft_version")}"
group = rootProject.property("maven_group").toString()

subprojects {
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

// Merge raw (unobfuscated) platform JARs
tasks.register<Jar>("mergeJars") {
    group = "build"
    description = "Merge raw Fabric and Paper JARs into one universal JAR"

    dependsOn(":fabric:shadowJar")
    dependsOn(":paper:shadowJar")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveFileName.set("${rootProject.name}-${project.version}-universal.jar")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    from(zipTree(project(":fabric").tasks.named<Jar>("shadowJar").flatMap { it.archiveFile })) {
        exclude("META-INF/MANIFEST.MF")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }

    from(zipTree(project(":paper").tasks.named<Jar>("shadowJar").flatMap { it.archiveFile })) {
        exclude("META-INF/MANIFEST.MF")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }

    manifest {
        attributes["Implementation-Version"] = project.version
    }
}

tasks.register<ProGuardTask>("obfuscate") {
    group = "build"
    description = "Obfuscate the universal merged JAR"

    dependsOn("mergeJars")

    injars(tasks.named<Jar>("mergeJars").flatMap { it.archiveFile })
    outjars(layout.buildDirectory.file("libs/${rootProject.name}-${project.version}.jar"))

    configuration(rootProject.file("proguard.pro"))

    libraryjars(
        files(
            project(":fabric").configurations["compileClasspath"],
            project(":paper").configurations["compileClasspath"]
        )
    )
}

tasks.register<Copy>("collectJars") {
    group = "build"
    description = "Copy all final JARs into the root build/libs folder"

    dependsOn(":fabric:shadowJar", ":paper:shadowJar", "obfuscate")

    from(project(":fabric").tasks.named<Jar>("shadowJar").map { it.archiveFile })
    from(project(":paper").tasks.named<Jar>("shadowJar").map { it.archiveFile })
    from(layout.buildDirectory.file("libs/${rootProject.name}-${project.version}.jar"))

    into(layout.buildDirectory.dir("libs"))
}

tasks.register("build") {
    group = "build"
    dependsOn("collectJars")
}
