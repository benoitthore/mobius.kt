plugins {
    kotlin("multiplatform")
}

apply(from = "../gradle/publishing.gradle.kts")

kotlin {
    ios()
    //watchos()
    tvos()
    macosX64("macos")
    linuxX64("linux")
    mingwX64("windows")
    jvm()
    js(BOTH) {
        nodejs()
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.natpryce:hamkrest:1.8.0.1")
                implementation(project(":mobius-core"))
                implementation(project(":mobius-internal"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-js"))
            }
        }
    }
}
