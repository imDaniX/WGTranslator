plugins {
    id("buildlogic.core-and-platform")
}

dependencies {
    constraints {
        "implementation"(libs.snakeyaml) {
            because("Bukkit provides SnakeYaml")
        }
    }

    "api"(project(":worldguard-libs:core"))
    "api"(libs.worldedit.core)
    "implementation"(libs.flyway.core)
    "implementation"(libs.snakeyaml)
    "implementation"(libs.guava)
    "compileOnlyApi"(libs.jsr305)
    "compileOnlyApi"(libs.adventure.api)
    "compileOnlyApi"(libs.adventure.minimessage)
    "compileOnlyApi"(libs.adventure.serializer.legacy)
    "compileOnlyApi"(libs.adventure.serializer.gson)
    "compileOnlyApi"(libs.adventure.serializer.plain)
    "implementation"(libs.gson)

    "compileOnly"(libs.worldedit.libs.ap)
    "annotationProcessor"(libs.worldedit.libs.ap)
    // ensure this is on the classpath for the AP
    "annotationProcessor"(libs.guava)

    "compileOnly"(libs.autoService) {
        because("Needed to resolve annotations in Piston")
    }

    "testImplementation"(libs.hamcrest.library)
    "testImplementation"(libs.adventure.api)
    "testImplementation"(libs.adventure.minimessage)
    "testImplementation"(libs.adventure.serializer.legacy)
    "testImplementation"(libs.adventure.serializer.gson)
    "testImplementation"(libs.adventure.serializer.plain)
}

tasks.compileJava {
    dependsOn(":worldguard-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}
