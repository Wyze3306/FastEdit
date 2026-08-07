plugins {
    java
    id("com.gradleup.shadow") version "8.3.7"
}

group = "fr.fastedit"
version = "1.3.0"
description = "FastEdit — async WorldEdit plugin for PowerNukkitX (Bedrock)"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://repo.powernukkitx.cn/releases")
    maven("https://repo.powernukkitx.cn/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    // PNX 3 is not published to any reachable Maven repo, so the compile
    // classpath comes from a local build: FASTEDIT_PNX_JAR, else the sibling
    // PowerNukkitX checkout's build/libs (plain jar + its dependency jars).
    val localPnx = System.getenv("FASTEDIT_PNX_JAR")
        ?: System.getenv("LINESIA_PNX_JAR")
        ?: rootProject.file("../PowerNukkitX/build/libs")
            .takeIf { it.isDirectory }?.absolutePath
    if (localPnx != null && file(localPnx).exists()) {
        val f = file(localPnx)
        if (f.isDirectory) {
            compileOnly(fileTree(f) { include("*.jar") })
        } else {
            compileOnly(files(localPnx))
            val siblingLibs = f.parentFile
            if (siblingLibs != null && siblingLibs.isDirectory) {
                compileOnly(fileTree(siblingLibs) { include("*.jar"); exclude(f.name) })
            }
        }
    } else {
        compileOnly("org.powernukkitx:powernukkitx:3.0.1")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.build { dependsOn(tasks.shadowJar) }
