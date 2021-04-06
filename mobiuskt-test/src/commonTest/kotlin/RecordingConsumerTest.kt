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

import kt.mobius.test.RecordingConsumer
import org.awaitility.Awaitility.await

class RecordingConsumerTest {
    private var consumer: RecordingConsumer<String>? = null

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        consumer = RecordingConsumer()
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldSupportClearingValues() {
        consumer!!.accept("to be cleared")
        consumer!!.clearValues()
        consumer!!.accept("this!")
        consumer!!.assertValues("this!")
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldTerminateWaitEarlyOnChange() {
        val waitResult: java.util.concurrent.atomic.AtomicReference<Boolean> = java.util.concurrent.atomic.AtomicReference<Boolean>()

        // given a thread that is blocked waiting for the consumer to get a value
        val t: java.lang.Thread = java.lang.Thread(
                java.lang.Runnable { waitResult.set(consumer!!.waitForChange(100000)) })
        t.start()
        await().atMost(java.time.Duration.ofSeconds(5)).until { t.getState() == java.lang.Thread.State.TIMED_WAITING }

        // when a value arrives
        consumer!!.accept("heya")

        // then, in less than 1/10th of the configured waiting time,
        await().atMost(java.time.Duration.ofSeconds(10)).until { waitResult.get() != null }

        // the result is 'true'
        assertThat(waitResult.get(), `is`(true))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldReturnTrueForNoChange() {
        assertThat(consumer!!.waitForChange(50), `is`(true))
    }
}