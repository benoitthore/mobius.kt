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

import org.hamcrest.Matchers.allOf

/** Provides utility functions for matching [Next] instances in tests.  */
object NextMatchers {
    /**
     * Returns a matcher that matches [Next] instances without a model.
     *
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasNoModel(): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>?>() {
            protected fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                return if (item.hasModel()) {
                    mismatchDescription.appendText("it had a model: " + item.modelUnsafe())
                    false
                } else {
                    true
                }
            }

            fun describeTo(description: Description) {
                description.appendText("Next without model")
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances with a model.
     *
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasModel(): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>?>() {
            protected fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (!item.hasModel()) {
                    mismatchDescription.appendText("it had no model")
                    return false
                }
                return true
            }

            fun describeTo(description: Description) {
                description.appendText("Next with any model")
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances with a model that is equal to the
     * supplied one.
     *
     * @param expected the expected model
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasModel(expected: M): Matcher<Next<M, F>> {
        return hasModel(equalTo(expected))
    }

    /**
     * Returns a matcher that matches [Next] instances with a model that matches the supplied
     * model matcher.
     *
     * @param matcher the matcher to apply to the model
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasModel(matcher: Matcher<M>): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>?>() {
            protected fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                return if (!item.hasModel()) {
                    mismatchDescription.appendText("it had no model")
                    false
                } else if (!matcher.matches(item.modelUnsafe())) {
                    mismatchDescription.appendText("the model ")
                    matcher.describeMismatch(item.modelUnsafe(), mismatchDescription)
                    false
                } else {
                    true
                }
            }

            fun describeTo(description: Description) {
                description.appendText("Next with model ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances with no effects.
     *
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasNoEffects(): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>?>() {
            protected fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                return if (item.hasEffects()) {
                    mismatchDescription.appendText("it had effects: " + item.effects())
                    false
                } else {
                    true
                }
            }

            fun describeTo(description: Description) {
                description.appendText("Next without effects")
            }
        }
    }

    /**
     * Returns a matcher that matches [Next] instances whose effects match the supplied effect
     * matcher.
     *
     * @param matcher the matcher to apply to the effects
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasEffects(matcher: Matcher<Iterable<F>?>): Matcher<Next<M, F>> {
        return object : TypeSafeDiagnosingMatcher<Next<M, F>?>() {
            protected fun matchesSafely(item: Next<M, F>, mismatchDescription: Description): Boolean {
                if (!item.hasEffects()) {
                    mismatchDescription.appendText("it had no effects")
                    return false
                } else if (!matcher.matches(item.effects())) {
                    mismatchDescription.appendText("the effects were ")
                    matcher.describeMismatch(item.effects(), mismatchDescription)
                    return false
                }
                return true
            }

            fun describeTo(description: Description) {
                description.appendText("Next with effects ").appendDescriptionOf(matcher)
            }
        }
    }

    /**
     * Returns a matcher that matches if all the supplied effects are present in the supplied [ ], in any order. The [Next] may have more effects than the ones included.
     *
     * @param effects the effects to match (possibly empty)
     * @param <M> the model type
     * @param <F> the effect type
     * @return a matcher that matches [Next] instances that include all the supplied effects
    </F></M> */
    @java.lang.SafeVarargs
    fun <M, F> hasEffects(vararg effects: F): Matcher<Next<M, F>> {
        return hasEffects(hasItems(effects))
    }

    /**
     * Returns a matcher that matches [Next] instances with no model and no effects.
     *
     * @param <M> the model type
     * @param <F> the effect type
    </F></M> */
    fun <M, F> hasNothing(): Matcher<Next<M, F>> {
        return allOf(hasNoModel<Any, Any>(), hasNoEffects<Any, Any>())
    }
}