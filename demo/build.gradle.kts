plugins {
    kotlin("multiplatform")
    application
}

application {
    mainClass.set("demo.MainKt")
}

kotlin {
    jvm {
        withJava()
    }
    macosX64("macos") {
        binaries {
            executable {
                entryPoint = "demo.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":mobius-core"))
            }
        }
    }
}
