plugins {
    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.run.paper) apply false
    alias(libs.plugins.paperweight) apply false
    alias(libs.plugins.shadow) apply false
    `maven-publish`
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
    }
}

// Merge platform JARs
tasks.register<Jar>("mergeJars") {
    group = "build"
    description = "Merge raw Fabric and Paper JARs into one universal JAR"

    dependsOn(":fabric:shadowJar")
    dependsOn(":paper:shadowJar")

    isZip64 = true

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

tasks.register<Copy>("collectJars") {
    group = "build"
    description = "Copy all final JARs into the root build/libs folder"

    dependsOn(":fabric:shadowJar", ":paper:shadowJar", "mergeJars")

    from(project(":fabric").tasks.named<Jar>("shadowJar").map { it.archiveFile })
    from(project(":paper").tasks.named<Jar>("shadowJar").map { it.archiveFile })
    from(layout.buildDirectory.file("libs/${rootProject.name}-${project.version}.jar"))

    into(layout.buildDirectory.dir("libs"))
}

tasks.register("build") {
    group = "build"
    dependsOn("collectJars")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.named("mergeJars"))
            groupId = project.group.toString()
            artifactId = project.property("archives_base_name") as String + "-universal"
            version = project.version.toString()
        }
    }
    repositories {

    }
}