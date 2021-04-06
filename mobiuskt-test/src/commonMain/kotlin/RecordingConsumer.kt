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

import org.hamcrest.CoreMatchers.equalTo

class RecordingConsumer<V> : Consumer<V> {
    private val values: MutableList<V> = java.util.ArrayList<V>()
    private val lock = Any()
    fun accept(value: V) {
        synchronized(lock) {
            values.add(value)
            lock.notify()
        }
    }

    fun waitForChange(timeoutMs: Long): Boolean {
        synchronized(lock) {
            var now: Long = java.lang.System.nanoTime()
            val deadline: Long = now + java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(timeoutMs)
            return try {
                val valuesBefore = values.size
                while (values.size == valuesBefore && now < deadline) {
                    lock.wait(timeoutMs)
                    now = java.lang.System.nanoTime()
                }
                true
            } catch (e: java.lang.InterruptedException) {
                false
            }
        }
    }

    fun valueCount(): Int {
        synchronized(lock) { return values.size }
    }

    @java.lang.SafeVarargs
    fun assertValues(vararg expectedValues: V) {
        synchronized(lock) { assertThat(values, equalTo(java.util.Arrays.asList(*expectedValues))) }
    }

    @java.lang.SafeVarargs
    fun assertValuesInAnyOrder(vararg expectedValues: V) {
        synchronized(lock) { assertThat(values, containsInAnyOrder(expectedValues)) }
    }

    fun clearValues() {
        synchronized(lock) { values.clear() }
    }
}