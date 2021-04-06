/*
 * -\-\-
 * Mobius
 * --
 * Copyright (c) 2017-2020 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */
package kt.mobius.test

import com.spotify.mobius.runners.WorkRunner

class TestWorkRunner : WorkRunner {
    private val queue: java.util.Queue<java.lang.Runnable> = java.util.ArrayDeque<java.lang.Runnable>()
    var isDisposed = false
        private set

    fun post(runnable: java.lang.Runnable?) {
        synchronized(queue) {
            if (isDisposed) {
                throw java.lang.IllegalStateException("this WorkRunner has already been disposed")
            }
            queue.add(runnable)
        }
    }

    private fun runOne() {
        var runnable: java.lang.Runnable
        synchronized(queue) {
            if (queue.isEmpty()) return
            runnable = queue.remove()
        }
        runnable.run()
    }

    fun runAll() {
        while (true) {
            synchronized(queue) { if (queue.isEmpty()) return }
            runOne()
        }
    }

    fun dispose() {
        synchronized(queue) {
            isDisposed = true
            queue.clear()
        }
    }
}