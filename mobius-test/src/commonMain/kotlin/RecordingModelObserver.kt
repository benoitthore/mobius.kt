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

import com.spotify.mobius.functions.Consumer

class RecordingModelObserver<S> : Consumer<S> {
    private val recorder: RecordingConsumer<S> = RecordingConsumer<S>()
    fun accept(newModel: S) {
        recorder.accept(newModel)
    }

    fun waitForChange(timeoutMs: Long): Boolean {
        return recorder.waitForChange(timeoutMs)
    }

    fun valueCount(): Int {
        return recorder.valueCount()
    }

    @java.lang.SafeVarargs
    fun assertStates(vararg expectedStates: S) {
        recorder.assertValues(*expectedStates)
    }
}