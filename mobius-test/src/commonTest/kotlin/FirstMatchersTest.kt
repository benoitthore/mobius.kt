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

class FirstMatchersTest {
    private var first: First<String, Int>? = null
    private var matcher: Matcher<First<String, Int>>? = null
    private var desc: Description? = null

    @Before
    @Throws(java.lang.Exception::class)
    fun setUp() {
        desc = StringDescription()
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelSpecific() {
        first = first("a")
        matcher = hasModel(equalTo("a"))
        assertTrue(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("has model: was \"a\"", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelWithValue() {
        first = first("a")
        matcher = hasModel("a")
        assertTrue(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("has model: was \"a\"", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasModelSpecificButWrong() {
        first = first("b")
        matcher = hasModel(equalTo("a"))
        assertFalse(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("bad model: was \"b\"", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNoEffectsMatch() {
        first = first("a")
        matcher = hasNoEffects()
        assertTrue(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("no effects", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasNoEffectsMismatch() {
        first = first("a", effects(1, 2, 3))
        matcher = hasNoEffects()
        assertFalse(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("has effects", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsSpecific() {
        first = first("a", effects(1, 3, 2))
        matcher = hasEffects(hasItems(1, 2, 3))
        assertTrue(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("has effects: ", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsSpecificButWrong() {
        first = first("a", effects(1))
        matcher = hasEffects(hasItems(2))
        assertFalse(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("bad effects: a collection containing <2> was <1>", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsSpecificButMissing() {
        first = first("a")
        matcher = hasEffects(hasItems(1, 2, 3))
        assertFalse(matcher.matches(first))
        matcher.describeMismatch(first, desc)
        assertEquals("no effects", desc.toString())
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesMatch() {
        first = first("a", effects(94))
        matcher = hasEffects(94)
        assertTrue(matcher.matches(first))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesMismatch() {
        first = first("a", effects(94))
        matcher = hasEffects(74)
        assertFalse(matcher.matches(first))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesPartialMatch() {
        first = first("a", effects(94, 74))
        matcher = hasEffects(94)
        assertTrue(matcher.matches(first))
    }

    @Test
    @Throws(java.lang.Exception::class)
    fun testHasEffectsWithConcreteInstancesOutOfOrder() {
        first = first("a", effects(94, 74, 65))
        matcher = hasEffects(65, 94, 74)
        assertTrue(matcher.matches(first))
    }
}