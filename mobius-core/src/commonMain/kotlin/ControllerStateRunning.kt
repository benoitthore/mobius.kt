package kt.mobius

import kotlinx.coroutines.flow.collect

class ControllerStateRunning<M, E, F>(
    private val actions: ControllerActions<M, E>,
    private val renderer: Connection<M>,
    loopFactory: MobiusLoop.Factory<M, E, F>,
    private val startModel: M
) : ControllerStateBase<M, E>() {

    private val loop = loopFactory.startFrom(startModel)

    override val stateName: String = "running"
    override val isRunning: Boolean = true

    suspend fun start() {
        loop.observe().collect {
            actions.postUpdateView(it)
        }
    }

    override fun onDispatchEvent(event: E) {
        loop.dispatchEvent(event)
    }

    override fun onUpdateView(model: M) {
        renderer.accept(model)
    }

    override fun onStop() {
        loop.dispose()
        val mostRecentModel = loop.mostRecentModel
        actions.goToStateCreated(renderer, mostRecentModel)
    }

    override fun onGetModel(): M {
        val model = loop.mostRecentModel
        return model ?: startModel
    }
}
