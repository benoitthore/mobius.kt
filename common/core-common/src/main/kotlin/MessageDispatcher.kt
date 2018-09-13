package com.spotify.mobius

import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.runners.Runnable
import com.spotify.mobius.runners.WorkRunner

/**
 * Dispatches messages to a given runner.
 *
 * @param M message type (typically a model, event, or effect descriptor type)
 */
internal class MessageDispatcher<M>(
    val runner: WorkRunner,
    val consumer: Consumer<M>
) : Consumer<M>, Disposable {

  override fun accept(message: M) {
    runner.post(
        object : Runnable {
          override fun run() {
            try {
              consumer.accept(message)

            } catch (throwable: Throwable) {
              println("Consumer threw an exception when accepting message: $message")
              println(throwable.message)
            }
          }
        })
  }

  override fun dispose() {
    runner.dispose()
  }
}
