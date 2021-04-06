rootProject.name = "mobiuskt"

enableFeaturePreview("GRADLE_METADATA")

include(
    ":mobiuskt-core",
    ":mobiuskt-extras",
    ":mobiuskt-internal",
    ":mobiuskt-test",
    ":jvm:mobiuskt-android"
)

include(":mobiuskt-coroutines")

// Samples
//include ":samples:todo:todo-common"
//include ":samples:todo:todo-ios"
//include ":samples:todo:todo-web"
