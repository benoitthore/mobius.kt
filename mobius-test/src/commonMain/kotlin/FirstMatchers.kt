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

import kt.mobius.First

//import com.spotify.mobius.First
//import org.hamcrest.Description
//import org.hamcrest.Matcher
//import org.hamcrest.Matchers.equalTo
//import org.hamcrest.Matchers.hasItems
//import org.hamcrest.TypeSafeDiagnosingMatcher

import com.natpryce.hamkrest.*

/** Provides utility functions for matching against [First] instances.  */
object FirstMatchers {
    /**
     * Returns a matcher that matches [First] instances with a model that is equal to the
     * supplied one.
     *
     * @param expected the expected model
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasModel(expected: M): Matcher<First<M, F>> {
        return hasModel(equalTo(expected))
    }

    /**
     * Returns a matcher that matches [First] instances with a model that matches the supplied
     * model matcher.
     *
     * @param matcher the matcher to apply to the model
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasModel(matcher: Matcher<M>): Matcher<First<M, F>> {
        return object : TypeSafeDiagnosingMatcher<First<M, F>?>() {
            protected fun matchesSafely(item: First<M, F>, mismatchDescription: Description): Boolean {
                return if (!matcher.matches(item.model())) {
                    mismatchDescription.appendText("bad model: ")
                    matcher.describeMismatch(item.model(), mismatchDescription)
                    false
                } else {
                    mismatchDescription.appendText("has model: ")
                    matcher.describeMismatch(item.model(), mismatchDescription)
                    true
                }
            }

            fun describeTo(description: Description) {
                description.appendText("has a model: ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches [First] instances with no effects.
     *
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasNoEffects(): Matcher<First<M, F>> {
        return object : TypeSafeDiagnosingMatcher<First<M, F>?>() {
            protected fun matchesSafely(item: First<M, F>, mismatchDescription: Description): Boolean {
                return if (item.hasEffects()) {
                    mismatchDescription.appendText("has effects")
                    false
                } else {
                    mismatchDescription.appendText("no effects")
                    true
                }
            }

            fun describeTo(description: Description) {
                description.appendText("should have no effects")
            }
        }
    }

    /**
     * Returns a matcher that matches [First] instances whose effects match the supplied effect
     * matcher.
     *
     * @param matcher the matcher to apply to the effects
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasEffects(matcher: Matcher<Iterable<F>?>): Matcher<First<M, F>> {
        return object : TypeSafeDiagnosingMatcher<First<M, F>?>() {
            protected fun matchesSafely(item: First<M, F>, mismatchDescription: Description): Boolean {
                return if (!item.hasEffects()) {
                    mismatchDescription.appendText("no effects")
                    false
                } else if (!matcher.matches(item.effects())) {
                    mismatchDescription.appendText("bad effects: ")
                    matcher.describeMismatch(item.effects(), mismatchDescription)
                    false
                } else {
                    mismatchDescription.appendText("has effects: ")
                    matcher.describeMismatch(item.effects(), mismatchDescription)
                    true
                }
            }

            fun describeTo(description: Description) {
                description.appendText("has effects: ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches if all the supplied effects are present in the supplied [ ], in any order. The [First] may have more effects than the ones included.
     *
     * @param effects the effects to match (possibly empty)
     * @param <M> the model type
     * @param <F> the effect type
     * @return a matcher that matches [First] instances that include all the supplied effects
    </F></M> */
    @java.lang.SafeVarargs
    fun <M, F> hasEffects(vararg effects: F): Matcher<First<M, F>> {
        return hasEffects(hasItems(effects))
    }
}