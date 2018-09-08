package com.spotify.mobius.runners

import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/** A [WorkRunner] implementation that is backed by an [ExecutorService]. */
class ExecutorServiceWorkRunner(private val service: ExecutorService) : WorkRunner {

  override fun post(runnable: Runnable) {
    service.submit(runnable)
  }

  override fun dispose() {
    try {
      val runnables = service.shutdownNow()

      if (!runnables.isEmpty()) {
        println("Disposing ExecutorServiceWorkRunner with ${runnables.size} outstanding tasks.")
        //TODO: LOGGER.warn(
        //TODO:    "Disposing ExecutorServiceWorkRunner with {} outstanding tasks.", runnables.size)
      }

      if (!service.awaitTermination(100, TimeUnit.MILLISECONDS)) {
        println("ExecutorService shutdown timed out; there are still tasks executing")
        //TODO: LOGGER.error("ExecutorService shutdown timed out; there are still tasks executing")
      }
    } catch (e: InterruptedException) {
      println("Timeout when disposing work runner")
      println(e)
      //TODO: LOGGER.error("Timeout when disposing work runner", e)
    }
  }
}
