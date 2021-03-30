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

import com.google.auto.value.AutoValue

/** Defines the final state of a Mobius loop after a sequence of events have been processed.  */
@AutoValue
abstract class Result<M, F> {
    /**
     * Returns the final model - note that was not necessarily produced by the last Next, in case that
     * returned an empty model.
     */
    @Nonnull
    abstract fun model(): M

    /** Returns the Next that resulted from the last processed event  */
    @Nonnull
    abstract fun lastNext(): Next<M, F>?

    companion object {
        /**
         * Create a [Result] with the provided model and next.
         *
         * @param model the model the loop ended with
         * @param lastNext the last next emitted by the loop
         * @param <M> the model type
         * @param <F> the effect type
        </F></M> */
        fun <M, F> of(model: M, lastNext: Next<M, F>?): Result<M, F> {
            return AutoValue_Result(model, lastNext)
        }
    }
}