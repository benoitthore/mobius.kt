package demo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kt.mobius.Connectable
import kt.mobius.Connection
import kt.mobius.First
import kt.mobius.Init
import kt.mobius.Mobius
import kt.mobius.Next
import kt.mobius.SimpleLogger
import kt.mobius.Update
import kt.mobius.functions.Consumer

suspend fun run() {
    val factory = Mobius.loop(UpdateFunc, EffectHandler)
        .init(InitFunc)
        .logger(SimpleLogger("demo"))
    val controller = Mobius.controller(factory, Model())
    controller.connect(object : Connectable<Model, Event> {
        override fun connect(output: Consumer<Event>): Connection<Model> {
            return object : Connection<Model> {
                override fun accept(value: Model) = Unit

                override fun dispose() = Unit
            }
        }
    })
    controller.start()

    while (true) {
        delay(1000)
    }
}

data class Model(
    val count: Int = 0,
    val message: String = "ping",
)

sealed class Event {
    object Increment : Event()
    data class Message(val value: String) : Event()
}

sealed class Effect {
    object IncrementLater : Effect()
    data class GetNextMessage(val currentMessage: String) : Effect()
}

object InitFunc : Init<Model, Effect> {
    override fun init(model: Model): First<Model, Effect> {
        return First.first(
            model, setOf(
                Effect.IncrementLater,
                Effect.GetNextMessage(model.message)
            )
        )
    }
}

object UpdateFunc : Update<Model, Event, Effect> {

    override fun update(model: Model, event: Event): Next<Model, Effect> {
        return when (event) {
            Event.Increment -> Next.next(
                model.copy(count = model.count + 1),
                setOf<Effect>(Effect.IncrementLater)
            )
            is Event.Message -> Next.next(
                model.copy(message = event.value),
                setOf<Effect>(Effect.GetNextMessage(event.value))
            )
        }
    }
}

val EffectHandler = { effects: Flow<Effect> ->
    callbackFlow<Event> {
        effects.collect { effect ->
            when (effect) {
                Effect.IncrementLater -> launch {
                    delay(2300)
                    send(Event.Increment)
                }
                is Effect.GetNextMessage -> launch {
                    val nextMessage = when (effect.currentMessage) {
                        "ping" -> "pong"
                        else -> "ping"
                    }
                    delay(1200L)
                    send(Event.Message(nextMessage))
                }
            }
        }
    }
}
