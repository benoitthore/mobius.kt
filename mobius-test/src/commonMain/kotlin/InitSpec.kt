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
 * A class to help with Behavior Driven Testing of the [Init] function of a Mobius program.
 *
 * @param <M> model type
 * @param <F> effects type
</F></M> */
class InitSpec<M, F>(init: Init<M, F>?) {
    private val init: Init<M, F>
    fun `when`(model: M): Then<M, F> {
        checkNotNull(model)
        return object : Then<M, F> {
            override fun then(assertion: Assert<M, F>) {
                assertion.assertFirst(init.init(model))
            }

            override fun thenError(assertion: AssertError) {
                var error: java.lang.Exception? = null
                try {
                    init.init(model)
                } catch (e: java.lang.Exception) {
                    error = e
                }
                if (error == null) {
                    throw java.lang.AssertionError("An exception was expected but was not thrown")
                }
                assertion.assertError(error)
            }
        }
    }

    /** An alias for [.when] to be used with Kotlin  */
    fun whenInit(model: M): Then<M, F> {
        return `when`(model)
    }

    /**
     * The final step in a behavior test. Instances of this class will call your function under test
     * with the previously provided values (i.e. given and when) and will pass the result of the
     * function over to your [Assert] implementation. If you choose to call `thenError`,
     * your function under test will be invoked and any exceptions thrown will be caught and passed on
     * to your [AssertError] implementation. If no exceptions are thrown by the function under
     * test, then an [AssertError] will be thrown to fail the test.
     */
    interface Then<M, F> {
        /**
         * Runs the specified test and then runs the [Assert] on the resulting [First].
         *
         * @param assertion to compare the result with
         */
        fun then(assertion: Assert<M, F>?)

        /**
         * Runs the specified test and validates that it throws the exception expected by the supplied
         * [AssertError].
         *
         * @param assertion an expectation on the exception
         */
        fun thenError(assertion: AssertError?)
    }

    /** Interface for defining your error assertions.  */
    interface AssertError {
        fun assertError(e: java.lang.Exception?)
    }

    /** Interface for defining your assertions over [First] instances.  */
    interface Assert<M, F> {
        fun assertFirst(first: First<M, F>?)
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
        fun <M, F> assertThatFirst(vararg matchers: Matcher<First<M, F>?>?): Assert<M, F> {
            return Assert<M, F> { first: First<M, F>? ->
                for (matcher in matchers) {
                    assertThat(first, matcher)
                }
            }
        }
    }

    init {
        this.init = checkNotNull(init)
    }
}