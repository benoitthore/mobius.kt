import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("digital.wup:android-maven-publish:3.6.2")
    }
}

plugins {
    kotlin("multiplatform") version KOTLIN_VERSION apply false
    id("org.jetbrains.dokka") version DOKKA_VERSION
    kotlin("jvm") version "1.4.30"
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }

    System.getenv("GITHUB_REF")?.let { ref ->
        if (ref.startsWith("refs/tags/")) {
            version = ref.substringAfterLast("refs/tags/")
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaMultiModuleTask> {
    if (!name.contains("html", ignoreCase = true)) return@withType

    val docs = buildDir.resolve("dokka/html")
    outputDirectory.set(docs)
    doLast {
        docs.resolve("-modules.html").renameTo(docs.resolve("index.html"))
    }
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:$DOKKA_VERSION")
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}