package kt.mobius.util

import kotlinx.coroutines.CoroutineScope

actual fun runBlocking(block: suspend CoroutineScope.() -> Unit) =
    kotlinx.coroutines.runBlocking(block = block)
