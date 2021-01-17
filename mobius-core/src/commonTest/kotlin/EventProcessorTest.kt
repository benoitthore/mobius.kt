package kt.mobius

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kt.mobius.MobiusStore.Companion.create
import kt.mobius.util.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EventProcessorTest {
    private lateinit var underTest: EventProcessor<String, Int, Long>

    private lateinit var models: Flow<String>
    private lateinit var effects: SharedFlow<Long>

    @BeforeTest
    fun setUp() = runBlocking {
        underTest = EventProcessor(createStore())

        models = underTest.model.buffer(100)
        effects = underTest.effects.shareIn(GlobalScope, Eagerly, Int.MAX_VALUE)
    }

    @Test
    fun shouldEmitStateIfStateChanged() = runBlocking {
        underTest.init()
        underTest.update(1)
        val selected = models.take(2).toList()
        assertEquals(listOf("init!", "init!->1"), selected)
    }

    @Test
    fun shouldNotEmitStateIfStateNotChanged() = runBlocking {
        underTest.init()
        //stateConsumer.clearValues()
        //underTest.update(0)
        //stateConsumer.assertValues()
    }

    @Test
    fun shouldOnlyEmitStateStateChanged() = runBlocking {
        val actuals = models
            .onStart {
                underTest.init()
                assertEquals("init!", underTest.currentModel)
                underTest.update(0)
                assertEquals("init!", underTest.currentModel)
                underTest.update(1)
                assertEquals("init!->1", underTest.currentModel)
                underTest.update(0)
                assertEquals("init!->1", underTest.currentModel)
                underTest.update(2)
                assertEquals("init!->1->2", underTest.currentModel)
            }
            .take(3)
            .toList()

        val expected = listOf("init", "init!", "init!->1", "init!->1->2")
        assertEquals(expected, actuals)
    }

    @Test
    fun shouldEmitEffectsWhenStateChanges() = runBlocking {
        underTest.init()
        underTest.update(3)

        val effects = effects.take(3).toList()
        assertTrue(effects.containsAll(listOf(10L, 20L, 30L)))
    }

    @Test
    fun shouldEmitStateDuringInit() = runBlocking {
        underTest.init()
        assertEquals("init!", models.first())
    }

    @Test
    fun shouldEmitEffectsDuringInit() = runBlocking {
        underTest.init()

        val values = effects.take(3).toList()
        assertTrue(values.containsAll(listOf(15L, 25L, 35L)))
    }

    @Test
    fun shouldQueueUpdatesReceivedBeforeInit() = runBlocking {
        underTest.update(1)
        underTest.update(2)
        underTest.update(3)

        assertEquals("init", models.first())
        underTest.init()

        val expected = listOf("init!", "init!->1", "init!->1->2", "init!->1->2->3")
        assertEquals(expected, models.take(4).toList())
    }

    @Test
    fun shouldDisallowDuplicateInitialisation() = runBlocking {
        assertFailsWith(IllegalStateException::class) {
            underTest.init()
        }
    }

    fun createStore(): MobiusStore<String, Int, Long> {
        return create(Init { model ->
            First.first("$model!", setOf(15L, 25L, 35L))
        }, Update { model: String, event: Int ->
            if (event == 0) {
                Next.noChange()
            } else {
                val effects = hashSetOf<Long>()
                for (i in 0 until event) {
                    effects.add(10L * (i + 1))
                }
                Next.next("$model->$event", effects)
            }
        }, "init")
    }
}
