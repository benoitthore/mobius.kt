package kt.mobius

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kt.mobius.functions.Consumer

class MobiusLoopController<M, E, F>(
    private val loopFactory: MobiusLoop.Factory<M, E, F>,
    private val defaultModel: M,
    private val mainThreadDispatcher: CoroutineDispatcher
) : MobiusLoop.Controller<M, E>, ControllerActions<M, E> {

    private var currentState: MutableStateFlow<ControllerStateBase<M, E>> =
        MutableStateFlow(ControllerStateInit(this, defaultModel))

    private val scope = CoroutineScope(mainThreadDispatcher + SupervisorJob())

    override val isRunning: Boolean
        get() = currentState.value.isRunning

    override val model: M
        get() = currentState.value.onGetModel()

    private fun dispatchEvent(event: E) {
        currentState.value.onDispatchEvent(event)
    }

    private fun updateView(model: M) {
        scope.launch {
            currentState.value.onUpdateView(model)
        }
    }

    override fun connect(view: Connectable<M, E>) {
        currentState.value.onConnect(view)
    }

    override fun disconnect() {
        currentState.value.onDisconnect()
    }

    override fun start() {
        currentState.value.onStart()
    }

    override fun stop() {
        currentState.value.onStop()
    }

    override fun replaceModel(model: M) {
        currentState.value.onReplaceModel(model)
    }

    override fun postUpdateView(model: M) {
        updateView(model)
    }

    override fun goToStateInit(nextModelToStartFrom: M) {
        currentState.value = ControllerStateInit(this, nextModelToStartFrom)
    }

    override fun goToStateCreated(renderer: Connection<M>, nextModelToStartFrom: M?) {
        val nextModel = nextModelToStartFrom ?: defaultModel
        currentState.value = ControllerStateCreated<M, E, F>(this, renderer, nextModel)
    }

    override fun goToStateCreated(view: Connectable<M, E>, nextModelToStartFrom: M) {
        val safeModelHandler = SafeConnectable(view)

        val modelConnection = safeModelHandler.connect(
            Consumer { event ->
                dispatchEvent(event)
            })

        goToStateCreated(modelConnection, nextModelToStartFrom)
    }

    override fun goToStateRunning(renderer: Connection<M>, nextModelToStartFrom: M) {
        val stateRunning = ControllerStateRunning(this, renderer, loopFactory, nextModelToStartFrom)

        currentState.value = stateRunning

        scope.launch { stateRunning.start() }
    }
}
