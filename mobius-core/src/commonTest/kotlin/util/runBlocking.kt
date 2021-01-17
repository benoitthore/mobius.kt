package kt.mobius.util

import kotlinx.coroutines.CoroutineScope

expect fun runBlocking(block: suspend CoroutineScope.() -> Unit)
