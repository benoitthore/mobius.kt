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

class NextMatchersTest {
    private var next: Next<String, Int>? = null
    private var matcher: Matcher<Next<String, Int>>? = null
    private var desc: Description? = null

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        desc = StringDescription()
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNothing() {
        matcher = hasNothing()
        assertFalse(matcher.matches(null))
        assertFalse(matcher.matches(next("1234")))
        assertFalse(matcher.matches(dispatch(effects("f1"))))
        assertFalse(matcher.matches(next("123", effects("f1"))))
        assertTrue(matcher.matches(noChange()))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModel() {
        next = next("a")
        matcher = hasModel()
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelFail() {
        next = noChange()
        matcher = hasModel()
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("it had no model", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNoModel() {
        next = dispatch(effects(1, 2))
        matcher = hasNoModel()
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNoModelMismatch() {
        next = next("a")
        matcher = hasNoModel()
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("it had a model: a", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelSpecific() {
        next = next("a")
        matcher = hasModel(equalTo("a"))
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelWithValue() {
        next = next("a")
        matcher = hasModel("a")
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelSpecificButWrong() {
        next = next("b")
        matcher = hasModel(equalTo("a"))
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("the model was \"b\"", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelSpecificButMissing() {
        next = noChange()
        matcher = hasModel(equalTo("a"))
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("it had no model", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNoEffectsMatch() {
        next = noChange()
        matcher = hasNoEffects()
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNoEffectsMismatch() {
        next = dispatch(effects(1, 2, 3))
        matcher = hasNoEffects()
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("it had effects: [1, 2, 3]", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsSpecific() {
        next = dispatch(effects(1, 3, 2))
        matcher = hasEffects(hasItems(1, 2, 3))
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsSpecificButWrong() {
        next = dispatch(effects(1))
        matcher = hasEffects(hasItems(2))
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("the effects were a collection containing <2> was <1>", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsSpecificButMissing() {
        next = noChange()
        matcher = hasEffects(hasItems(1, 2, 3))
        assertFalse(matcher.matches(next))
        matcher.describeMismatch(next, desc)
        assertEquals("it had no effects", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesMatch() {
        next = dispatch(effects(94))
        matcher = hasEffects(94)
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesMismatch() {
        next = dispatch(effects(94))
        matcher = hasEffects(74)
        assertFalse(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesPartialMatch() {
        next = dispatch(effects(94, 74))
        matcher = hasEffects(94)
        assertTrue(matcher.matches(next))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesOutOfOrder() {
        next = dispatch(effects(94, 74, 65))
        matcher = hasEffects(65, 94, 74)
        assertTrue(matcher.matches(next))
    }
}