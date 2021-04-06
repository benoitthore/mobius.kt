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
import kt.mobius.test.InitSpec

class InitSpecTest {
    private var initSpec: InitSpec<String, Int>? = null

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        initSpec = InitSpec(INIT)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun initTests() {
        initSpec
                .`when`("Hello World")
                .then(
                        InitSpec.Assert<String, Int> { first: error.NonExistentClass ->
                            assertThat(first.model(), `is`("Hello WorldHello World"))
                            assertThat(first.effects(), contains(1, 2, 3))
                        })
        initSpec
                .`when`("bad model")
                .thenError(InitSpec.AssertError { error: error.NonExistentClass? -> assertThat(error, instanceOf(java.lang.IllegalStateException::class.java)) })
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun whenAndWhenModelYieldsSameResults() {
        val successAssertion: InitSpec.Assert<String, Int> = InitSpec.Assert<String, Int> { first: error.NonExistentClass ->
            assertThat(first.model(), `is`("Hello WorldHello World"))
            assertThat(first.effects(), contains(1, 2, 3))
        }
        initSpec!!.`when`("Hello World").then(successAssertion)
        initSpec!!.whenInit("Hello World").then(successAssertion)
        val failureAssertion: InitSpec.AssertError = InitSpec.AssertError { error: error.NonExistentClass? -> assertThat(error, instanceOf(java.lang.IllegalStateException::class.java)) }
        initSpec!!.`when`("bad model").thenError(failureAssertion)
        initSpec!!.whenInit("bad model").thenError(failureAssertion)
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun shouldFailIfExpectedErrorDoesntHappen() {
        assertThatThrownBy {
            initSpec
                    .`when`("no crash here")
                    .thenError(InitSpec.AssertError { error: error.NonExistentClass? -> assertThat(error, instanceOf(java.lang.IllegalStateException::class.java)) })
        }
                .isInstanceOf(java.lang.AssertionError::class.java)
                .hasMessage("An exception was expected but was not thrown")
    }

    companion object {
        private val INIT: Init<String, Int> = Init<String, Int> { model ->
            if ("bad model" == model) {
                throw java.lang.IllegalStateException("Bad Bad Model!")
            }
            First.first(model.concat(model), effects(1, 2, 3))
        }
    }
}