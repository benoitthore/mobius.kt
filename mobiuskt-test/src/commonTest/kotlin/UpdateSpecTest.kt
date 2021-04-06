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

import com.spotify.mobius.Effects.effects
import kt.mobius.test.Result
import kt.mobius.test.UpdateSpec

class UpdateSpecTest {
    private var updateSpec: UpdateSpec<String, String, Int>? = null

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        updateSpec = UpdateSpec(UPDATE)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun updateTests() {
        updateSpec
                .given("HELLO")
                .`when`("anything")
                .then(assertThatNext(hasModel("HELLO"), hasEffects(1, 2)))
        updateSpec!!.given("anything but hello").`when`("any event").then(assertThatNext(hasNothing()))
    }

    @Test
    fun whenAndWhenEventYieldSameResult() {
        val assertion: UpdateSpec.Assert<String, Int> = assertThatNext(hasModel("HELLO"), hasEffects(1, 2))
        updateSpec!!.given("HELLO").`when`("anything").then(assertion)
        updateSpec!!.given("HELLO").whenEvent("anything").then(assertion)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldSupportWhenMultipleEvents() {
        updateSpec = UpdateSpec<String, String, Int>(
                error.NonExistentClass { model, event ->
                    Next.next(
                            model.toString() + " - " + event, if (event.equals("three")) effects(8, 7, 4) else effects())
                })
        updateSpec!!
                .given("init")
                .`when`("one", "two", "three")
                .then(
                        UpdateSpec.Assert<String, Int> { result: Result<String?, Int?> ->
                            assertThat(result.model(), `is`("init - one - two - three"))
                            assertThat(result.lastNext(), hasEffects(7, 8, 4))
                        })
        updateSpec!!
                .given("init")
                .whenEvents("one", "two", "three")
                .then(
                        UpdateSpec.Assert<String, Int> { result: Result<String?, Int?> ->
                            assertThat(result.model(), `is`("init - one - two - three"))
                            assertThat(result.lastNext(), hasEffects(7, 8, 4))
                        })
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldFailAsExpectedForMultipleEventsLast() {
        updateSpec = UpdateSpec<String, String, Int>(error.NonExistentClass { model, event -> Next.next(model.toString() + " - " + event) })
        assertThatThrownBy {
            updateSpec!!
                    .given("init")
                    .`when`("one", "two", "three")
                    .then(UpdateSpec.Assert<String, Int> { last: Result<String?, Int?> -> assertThat(last.model(), `is`("wrong")) })
        }
                .isInstanceOf(java.lang.AssertionError::class.java)
                .hasMessageContaining(
                        """Expected: is "wrong"
     but: was "init - one - two - three"""")
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldSupportErrorValidation() {
        updateSpec = CRASH_SPEC
        updateSpec!!
                .given("hi")
                .`when`("ok", "crash")
                .thenError(UpdateSpec.AssertError { e: error.NonExistentClass -> assertThat(e.getMessage(), `is`("expected")) })
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldOnlyAcceptErrorsInLastStep() {
        updateSpec = CRASH_SPEC

        // should throw the crash exception from the first event and never get to the 'thenError' clause
        assertThatThrownBy {
            updateSpec!!
                    .given("hi")
                    .`when`("crash", "crash")
                    .thenError(UpdateSpec.AssertError { e: error.NonExistentClass -> assertThat(e.getMessage(), `is`("expected")) })
        }
                .isInstanceOf(java.lang.RuntimeException::class.java)
                .hasMessage("expected")
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldFailIfExpectedErrorDoesntHappen() {
        assertThatThrownBy {
            updateSpec
                    .given("hi")
                    .`when`("no crash here")
                    .thenError(UpdateSpec.AssertError { error: error.NonExistentClass? -> assertThat(error, instanceOf(java.lang.IllegalStateException::class.java)) })
        }
                .isInstanceOf(java.lang.AssertionError::class.java)
                .hasMessage("An exception was expected but was not thrown")
    }

    companion object {
        private val UPDATE: Update<String, String, Int> = label@ Update<String, String, Int> { model, event ->
            if ("HELLO" != model) {
                return@label Next.noChange()
            }
            Next.next(model.toUpperCase(), effects(1, 2))
        }
        private val CRASH_SPEC = UpdateSpec<String, String, Int>(
                error.NonExistentClass { model, event ->
                    if (event.equals("crash")) {
                        throw java.lang.RuntimeException("expected")
                    }
                    Next.next(model.toString() + "-" + event)
                })
    }
}