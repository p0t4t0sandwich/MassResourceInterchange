import java.time.Instant

plugins {
    id("java")
    id("maven-publish")
    id("idea")
    id("eclipse")
    alias(libs.plugins.blossom)
    alias(libs.plugins.spotless)
    alias(libs.plugins.unimined)
}

base {
    archivesName = modName
}

java.toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
java.sourceCompatibility = JavaVersion.toVersion(javaVersion)
java.targetCompatibility = JavaVersion.toVersion(javaVersion)

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

spotless {
    format("misc") {
        target("*.gradle", ".gitattributes", ".gitignore")
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
        licenseHeader("""/**
 * Copyright (c) 2025 $author - dylan@sperrer.ca
 * The project is Licensed under <a href="https://github.com/p0t4t0sandwich/MassResourceInterchange/blob/main/LICENSE">MIT</a>
 */
""")
    }
}

sourceSets {
    create("api")
    create("common") {
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
    create("neoforge")
    create("velocity") {
        compileClasspath += getByName("api").output
        compileClasspath += getByName("common").output
        runtimeClasspath += getByName("api").output
        runtimeClasspath += getByName("common").output
    }
}

configurations {
    val mainCompileOnly by creating
    named("compileOnly") {
        extendsFrom(getByName("apiCompileOnly"))
        extendsFrom(getByName("commonCompileOnly"))
        extendsFrom(getByName("neoforgeCompileOnly"))
        extendsFrom(getByName("velocityCompileOnly"))
    }
}

repositories {
    maven("https://maven.neuralnexus.dev/mirror")
}

// ------------------------------------------- Vanilla -------------------------------------------
unimined.minecraft {
    combineWith(sourceSets.getByName("api"))
    combineWith(sourceSets.getByName("common"))
    version(minecraftVersion)
    mappings {
        parchment(parchmentMinecraft, parchmentVersion)
        mojmap()
        devFallbackNamespace("official")
    }
    defaultRemapJar = false
}

tasks.jar {
    archiveClassifier.set("vanilla")
}

// ------------------------------------------- API -------------------------------------------
tasks.register<Jar>("apiJar") {
    archiveClassifier.set("api")
    from(sourceSets.getByName("api").output)
}

// ------------------------------------------- Common -------------------------------------------
tasks.register<Jar>("commonJar") {
    archiveClassifier.set("common")
    from(sourceSets.getByName("common").output)
}

// ------------------------------------------- NeoForge -------------------------------------------
unimined.minecraft(sourceSets.getByName("neoforge")) {
    combineWith(sourceSets.main.get())
    neoForge {
        loader(neoForgeVersion)
    }
    defaultRemapJar = true
}

// ------------------------------------------- Velocity -------------------------------------------
tasks.register<Jar>("velocityJar") {
    from(sourceSets.getByName("velocity").output)
    from(sourceSets.getByName("api").output)
    from(sourceSets.getByName("common").output)
}

// ------------------------------------------- Common -------------------------------------------
dependencies {
    implementation(libs.annotations)
    implementation(libs.mixin)
    "commonCompileOnly"("org.slf4j:slf4j-api:2.0.16")
    "velocityCompileOnly"("com.velocitypowered:velocity-api:$velocityVersion")
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

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modName,
                "Specification-Version" to version,
                "Specification-Vendor" to "SomeVendor",
                "Implementation-Version" to version,
                "Implementation-Vendor" to "SomeVendor",
                "Implementation-Timestamp" to Instant.now().toString(),
                "FMLCorePluginContainsFMLMod" to "true",
                "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
                "MixinConfigs" to "$modId.mixins.json,$modId.forge.mixins.json"
            )
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(listOf("README.md", "LICENSE")) {
        into("META-INF")
    }
}

tasks.build.get().dependsOn(
    "apiJar",
    "commonJar",
    "velocityJar",
    "spotlessApply",
)
