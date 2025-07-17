import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

import xyz.wagyourtail.unimined.api.minecraft.task.RemapJarTask

import java.time.Instant

plugins {
    id("java")
    id("maven-publish")
    id("idea")
    id("eclipse")
    alias(libs.plugins.blossom)
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    alias(libs.plugins.unimined)
}

base {
    archivesName = modName
}

java.toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
java.sourceCompatibility = JavaVersion.toVersion(javaVersion)
java.targetCompatibility = JavaVersion.toVersion(javaVersion)

spotless {
    format("misc") {
        target("*.gradle.kts", ".gitattributes", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    java {
        target("src/**/*.java", "src/**/*.java.peb")
        toggleOffOn()
        importOrder()
        removeUnusedImports()
        cleanthat()
        googleJavaFormat("1.24.0")
            .aosp()
            .formatJavadoc(true)
            .reorderImports(true)
        formatAnnotations()
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
        licenseHeader("""/**
 * Copyright (c) 2025 $author
 * This project is Licensed under <a href="$sourceUrl/blob/main/LICENSE">$license</a>
 */""")
    }
}

val api: SourceSet by sourceSets.creating
val common: SourceSet by sourceSets.creating {
    blossom.javaSources {
        property("mod_id", modId)
        property("mod_name", modName)
        property("version", version.toString())
        property("license", license)
        property("author", author)
        property("description", description)
        property("homepage_url", homepageUrl)
    }
}
val neoforge: SourceSet by sourceSets.creating
val velocity: SourceSet by sourceSets.creating {
    listOf(api, common).forEach { sourceSet ->
        compileClasspath += sourceSet.output
        runtimeClasspath += sourceSet.output
    }
}

val mainCompileOnly: Configuration by configurations.creating
configurations.compileOnly.get().extendsFrom(mainCompileOnly)
val apiCompileOnly: Configuration by configurations.getting
val commonCompileOnly: Configuration by configurations.getting
val commonImplementation: Configuration by configurations.getting
val commonRuntimeClasspath: Configuration by configurations.getting
val neoforgeCompileOnly: Configuration by configurations.getting
val velocityCompileOnly: Configuration by configurations.getting
listOf(neoforgeCompileOnly, velocityCompileOnly).forEach {
    it.extendsFrom(apiCompileOnly)
    it.extendsFrom(commonCompileOnly)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<RemapJarTask> {
    mixinRemap {
        enableBaseMixin()
        disableRefmap()
    }
}

repositories {
    maven("https://maven.neuralnexus.dev/mirror")
}

unimined.minecraft {
    combineWith(api)
    combineWith(common)
    version(minecraftVersion)
    mappings {
        parchment(parchmentMinecraft, parchmentVersion)
        mojmap()
        devFallbackNamespace("official")
    }
    defaultRemapJar = false
}

tasks.register<Jar>("apiJar") {
    archiveClassifier.set("api")
    from(api.output)
}

tasks.register<Jar>("commonJar") {
    archiveClassifier.set("common")
    from(common.output)
}

tasks.register<ShadowJar>("commonShadowJar") {
    archiveClassifier.set("common-shadow")
    configurations = listOf(commonRuntimeClasspath)
    enableRelocation = true
    relocationPrefix = "dev.neuralnexus.mri.libs"
    from(common.output)

    dependencies {
        include(dependency("com.zaxxer:HikariCP:6.3.0"))
    }
    exclude("module-info.class", "META-INF/maven/**")
    mergeServiceFiles()
    minimize()
}

unimined.minecraft(neoforge) {
    combineWith(sourceSets.main.get())
    neoForge {
        loader(neoForgeVersion)
    }
    defaultRemapJar = true
}

tasks.register<Jar>("velocityJar") {
    archiveClassifier.set("velocity")
    from(velocity.output)
}

dependencies {
    mainCompileOnly(libs.annotations)
    mainCompileOnly(libs.mixin)
    commonCompileOnly(libs.slf4j)
    commonImplementation("com.zaxxer:HikariCP:6.3.0")
    velocityCompileOnly("com.velocitypowered:velocity-api:$velocityVersion")
}

tasks.withType<ProcessResources> {
    filesMatching(listOf(
        "bungee.yml",
        "fabric.mod.json",
        "pack.mcmeta",
        "META-INF/mods.toml",
        "META-INF/neoforge.mods.toml",
        "plugin.yml",
        "paper-plugin.yml",
        "ignite.mod.json",
        "META-INF/sponge_plugins.json",
        "velocity-plugin.json"
    )) {
        expand(project.properties)
    }
}

tasks.jar {
    dependsOn("commonShadowJar")
    from(
        api.output,
        zipTree(tasks.getByName<Jar>("commonShadowJar").archiveFile.get().asFile),
        neoforge.output
    ) // velocity.output
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modName,
                "Specification-Version" to version,
                "Specification-Vendor" to "NeuralNexus",
                "Implementation-Version" to version,
                "Implementation-Vendor" to "NeuralNexus",
                "Implementation-Timestamp" to Instant.now().toString(),
                "FMLCorePluginContainsFMLMod" to "true",
                "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                "MixinConfigs" to "$modId.mixins.vanilla.json,$modId.mixins.forge.json"
            )
        )
    }
    from(listOf("README.md", "LICENSE")) {
        into("META-INF")
    }
}
tasks.build.get().dependsOn("spotlessApply")
