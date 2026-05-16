plugins {
    java
    id("com.gradleup.shadow") version "8.3.7"
}

group = "fr.fastedit"
version = "1.0.3"
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
    val localPnx = System.getenv("FASTEDIT_PNX_JAR") ?: System.getenv("LINESIA_PNX_JAR")
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
        compileOnly("cn.powernukkitx:powernukkitx:2.0.0-SNAPSHOT")
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
