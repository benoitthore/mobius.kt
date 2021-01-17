package kt.mobius

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kt.mobius.disposables.Disposable
import kt.mobius.functions.Producer
import kt.mobius.runners.WorkRunner
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Synchronized

/**
 * This is the main loop for Mobius.
 *
 * It hooks up all the different parts of the main Mobius loop, and dispatches messages
 * internally on the appropriate executors.
 */
class MobiusLoop<M, E, F> private constructor(
    startModel: M,
    private val init: Init<M, F>,
    private val update: Update<M, E, F>,
    effectHandler: (Flow<F>) -> Flow<E>,
    eventSource: Flow<E>,
    eventContext: CoroutineContext,
    effectContext: CoroutineContext,
    runtimeContext: CoroutineContext,
) : Disposable {

    companion object {

        @mpp.JvmStatic
        @mpp.JsName("create")
        fun <M, E, F> create(
            startModel: M,
            init: Init<M, F>,
            update: Update<M, E, F>,
            effectHandler: (Flow<F>) -> Flow<E>,
            eventSource: Flow<E>,
            eventContext: CoroutineContext,
            effectContext: CoroutineContext,
            runtimeContext: CoroutineContext,
        ): MobiusLoop<M, E, F> {
            return MobiusLoop(
                startModel,
                init,
                update,
                effectHandler,
                eventSource,
                eventContext,
                effectContext,
                runtimeContext
            )
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + runtimeContext)

    private val eventFlow = MutableSharedFlow<E>(
        extraBufferCapacity = Int.MAX_VALUE
    )
    private val effectFlow = MutableSharedFlow<F>(
        extraBufferCapacity = Int.MAX_VALUE
    )
    private val modelFlow = MutableStateFlow(startModel)

    val mostRecentModel: M?
        get() = modelFlow.value

    private val disposed = MutableStateFlow(false)

    init {
        effectHandler(effectFlow)
            .flowOn(effectContext)
            .onEach(eventFlow::emit)
            .flowOn(eventContext)
            .launchIn(scope)

        merge(eventSource, eventFlow)
            .onStart {
                val first = init.init(startModel)
                modelFlow.tryEmit(first.model())
                first.effects().forEach(effectFlow::tryEmit)
            }
            .onEach { event ->
                val next = update(modelFlow.value, event)
                if (next.hasModel()) {
                    modelFlow.value = next.modelUnsafe()
                }
                next.effects().forEach(effectFlow::tryEmit)
            }
            .flowOn(eventContext)
            .launchIn(scope)
    }

    fun dispatchEvent(event: E) {
        check(!disposed.value) {
            "This loop has already been disposed. You cannot dispatch events after disposal"
        }
        eventFlow.tryEmit(event)
    }

    /**
     * Add an observer of model changes to this loop. If [mostRecentModel] is non-null, the
     * observer will immediately be notified of the most recent model. The observer will be
     * notified of future changes to the model until the loop or the returned [Disposable] is
     * disposed.
     *
     * @param observer a non-null observer of model changes
     * @return a [Disposable] that can be used to stop further notifications to the observer
     * @throws NullPointerException if the observer is null
     * @throws IllegalStateException if the loop has been disposed
     */
    fun observe(): Flow<M> {
        check(!disposed.value) {
            "This loop has already been disposed. You cannot observe a disposed loop"
        }
        return disposed.transform { disposed ->
            if (!disposed) {
                emitAll(modelFlow)
            }
        }
    }

    @Synchronized
    override fun dispose() {
        disposed.value = true
        scope.cancel()
    }

    /**
     * Defines a fluent API for configuring a [MobiusLoop]. Implementations must be immutable,
     * making them safe to share between threads.
     *
     * @param M the model type
     * @param E the event type
     * @param F the effect type
     */
    interface Builder<M, E, F> : Factory<M, E, F> {

        /**
         * @return a new [Builder] with the supplied [Init], and the same values as the
         * current one for the other fields.
         */
        @mpp.JsName("init")
        fun init(init: Init<M, F>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied [EventSource], and the same values as
         * the current one for the other fields. NOTE: Invoking this method will replace the current
         * [EventSource] with the supplied one. If you want to pass multiple event sources,
         * please use [.eventSources].
         */
        @mpp.JsName("eventSource")
        fun eventSource(eventSource: Flow<E>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied logger, and the same values as the current
         * one for the other fields.
         */
        @mpp.JsName("logger")
        fun logger(logger: Logger<M, E, F>): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied event runner, and the same values as the
         * current one for the other fields.
         */
        @mpp.JsName("eventRunner")
        fun eventRunner(eventRunner: CoroutineContext): Builder<M, E, F>

        /**
         * @return a new [Builder] with the supplied effect runner, and the same values as the
         * current one for the other fields.
         */
        @mpp.JsName("effectRunner")
        fun effectRunner(effectRunner: CoroutineContext): Builder<M, E, F>
    }

    interface Factory<M, E, F> {
        /**
         * Start a [MobiusLoop] using this factory.
         *
         * @param startModel the model that the loop should start from
         * @return the started [MobiusLoop]
         */
        @mpp.JsName("startFrom")
        fun startFrom(startModel: M): MobiusLoop<M, E, F>
    }

    /**
     * Defines a controller that can be used to start and stop MobiusLoops.
     *
     * If a loop is stopped and then started again, the new loop will continue from where the last
     * one left off.
     */
    interface Controller<M, E> {
        /**
         * Indicates whether this controller is running.
         *
         * @return true if the controller is running
         */
        val isRunning: Boolean

        /**
         * Get the current model of the loop that this controller is running, or the most recent model
         * if it's not running.
         *
         * @return a model with the state of the controller
         */
        val model: M

        /**
         * Connect a view to this controller.
         *
         * Must be called before [start].
         *
         * The [Connectable] will be given an event consumer, which the view should use to send
         * events to the MobiusLoop. The view should also return a [Connection] that accepts
         * models and renders them. Disposing the connection should make the view stop emitting events.
         *
         * The view Connectable is guaranteed to only be connected once, so you don't have to check
         * for multiple connections or throw [ConnectionLimitExceededException].
         *
         * @throws IllegalStateException if the loop is running or if the controller already is
         * connected
         */
        @mpp.JsName("connect")
        fun connect(view: Connectable<M, E>)

        /**
         * Disconnect UI from this controller.
         *
         * @throws IllegalStateException if the loop is running or if there isn't anything to disconnect
         */
        fun disconnect()

        /**
         * Start a MobiusLoop from the current model.
         *
         * @throws IllegalStateException if the loop already is running or no view has been connected
         */
        fun start()

        /**
         * Stop the currently running MobiusLoop.
         *
         * When the loop is stopped, the last model of the loop will be remembered and used as the
         * first model the next time the loop is started.
         *
         * @throws IllegalStateException if the loop isn't running
         */
        fun stop()

        /**
         * Replace which model the controller should start from.
         *
         * @param model the model with the state the controller should start from
         * @throws IllegalStateException if the loop is running
         */
        @mpp.JsName("replaceModel")
        fun replaceModel(model: M)
    }

    /** Interface for logging init and update calls.  */
    interface Logger<M, E, F> {
        /**
         * Called right before the [Init.init] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the init function.
         *
         * @param model the model that will be passed to the init function
         */
        @mpp.JsName("beforeInit")
        fun beforeInit(model: M)

        /**
         * Called right after the [Init.init] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the init function.
         *
         * @param model the model that was passed to init
         * @param result the [First] that init returned
         */
        @mpp.JsName("afterInit")
        fun afterInit(model: M, result: First<M, F>)

        /**
         * Called if the [Init.init] invocation throws an exception. This is a programmer
         * error; Mobius is in an undefined state if it happens.
         *
         * @param model the model object that led to the exception
         * @param exception the thrown exception
         */
        @mpp.JsName("exceptionDuringInit")
        fun exceptionDuringInit(model: M, exception: Throwable)

        /**
         * Called right before the [Update.update] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the update function.
         *
         * @param model the model that will be passed to the update function
         * @param event the event that will be passed to the update function
         */
        @mpp.JsName("beforeUpdate")
        fun beforeUpdate(model: M, event: E)

        /**
         * Called right after the [Update.update] function is called.
         *
         *
         * This method mustn't block, as it'll hinder the loop from running. It will be called on the
         * same thread as the update function.
         *
         * @param model the model that was passed to update
         * @param event the event that was passed to update
         * @param result the [Next] that update returned
         */
        @mpp.JsName("afterUpdate")
        fun afterUpdate(model: M, event: E, result: Next<M, F>)

        /**
         * Called if the [Update.update] invocation throws an exception. This is a
         * programmer error; Mobius is in an undefined state if it happens.
         *
         * @param model the model object that led to the exception
         * @param exception the thrown exception
         */
        @mpp.JsName("exceptionDuringUpdate")
        fun exceptionDuringUpdate(model: M, event: E, exception: Throwable)
    }
}
