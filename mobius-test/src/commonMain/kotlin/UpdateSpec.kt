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

/**
 * A class to help with Behavior Driven Testing of the [Update] function of a Mobius program.
 *
 * @param <M> model type
 * @param <E> events type
 * @param <F> effects type
</F></E></M> */
class UpdateSpec<M, E, F>(update: Update<M, E, F>?) {
    private val update: Update<M, E, F>
    fun given(model: M): When {
        return When(model)
    }

    inner class When private constructor(model: M) {
        private val model: M

        /**
         * Defines the event(s) that should be executed when the test is run. Events are executed in the
         * order supplied.
         *
         * @param event the first events
         * @param events the following events, possibly none
         * @return a [Then] instance for the remainder of the spec
         */
        @java.lang.SafeVarargs
        fun `when`(event: E, vararg events: E): Then<M, F> {
            return ThenImpl(model, event, *events)
        }

        /**
         * Defines the event that should be executed when the test is run. Events are executed in the
         * order supplied. This method is just an alias to [.when] for use with Kotlin
         *
         * @param event the first events
         * @return a [Then] instance for the remainder of the spec
         */
        fun whenEvent(event: E): Then<M, F> {
            return `when`(event)
        }

        /**
         * Defines the event(s) that should be executed when the test is run. Events are executed in the
         * order supplied. This method is just an alias to [.when] for use with Kotlin
         *
         * @param event the first events
         * @param events the following events, possibly none
         * @return a [Then] instance for the remainder of the spec
         */
        @java.lang.SafeVarargs
        fun whenEvents(event: E, vararg events: E): Then<M, F> {
            return `when`(event, *events)
        }

        init {
            this.model = checkNotNull(model)
        }
    }

    /**
     * The final step in a behavior test. Instances of this class will call your function under test
     * with the previously provided values (i.e. given and when) and will pass the result of the
     * function over to your [Assert] implementation. If you choose to call `thenError`,
     * your function under test will be invoked and any exceptions thrown will be caught and passed on
     * to your [AssertionError] implementation. If no exceptions are thrown by the function
     * under test, then an [AssertionError] will be thrown to fail the test.
     */
    interface Then<M, F> {
        /**
         * Runs the specified test and then invokes the [Assert] on the [Result].
         *
         * @param assertion to compare the result with
         */
        fun then(assertion: Assert<M, F>?)

        /**
         * Runs the specified test and validates that the last step throws the exception expected by the
         * supplied [AssertError]. Note that if the test specification has multiple events, it
         * will fail if the exception is thrown before the execution of the last event.
         *
         * @param assertion an expectation on the exception
         */
        fun thenError(assertion: AssertError?)
    }

    /** Interface for defining your error assertions.  */
    interface AssertError {
        fun assertError(e: java.lang.Exception?)
    }

    /** Interface for defining your assertions over [Next] instances.  */
    interface Assert<M, F> {
        fun apply(result: kt.mobius.test.Result<M, F>?)
    }

    private inner class ThenImpl @java.lang.SafeVarargs private constructor(model: M, event: E, vararg events: E) : Then<M, F> {
        private val model: M
        private val events: MutableList<E>
        override fun then(assertion: Assert<M, F>) {
            var last: Next<M, F>? = null
            var lastModel = model
            for (event in events) {
                last = update.update(lastModel, event)
                lastModel = last.modelOrElse(lastModel)
            }
            assertion.apply(kt.mobius.test.Result.Companion.of<M, F>(lastModel, checkNotNull(last)))
        }

        override fun thenError(assertion: AssertError) {
            var error: java.lang.Exception? = null
            var lastModel = model

            // play all events up to the last one
            for (i in 0 until events.size - 1) {
                lastModel = update.update(lastModel, events[i]).modelOrElse(lastModel)
            }

            // then, do the assertion on the final event
            try {
                update.update(model, events[events.size - 1])
            } catch (e: java.lang.Exception) {
                error = e
            }
            if (error == null) {
                throw java.lang.AssertionError("An exception was expected but was not thrown")
            }
            assertion.assertError(error)
        }

        init {
            this.model = checkNotNull(model)
            this.events = java.util.ArrayList<E>(events.size + 1)
            this.events.add(event)
            this.events.addAll(java.util.Arrays.asList(*events))
        }
    }

    companion object {
        /**
         * Convenience function for creating assertions.
         *
         * @param matchers an array of matchers, all of which must match
         * @param <M> the model type
         * @param <F> the effect type
         * @return an [Assert] that applies all the matchers
        </F></M> */
        @java.lang.SafeVarargs
        fun <M, F> assertThatNext(vararg matchers: Matcher<Next<M, F>?>?): Assert<M, F> {
            return Assert<M, F> { result: kt.mobius.test.Result<M, F> ->
                for (matcher in matchers) {
                    assertThat(result.lastNext(), matcher)
                }
            }
        }
    }

    init {
        this.update = checkNotNull(update)
    }
}