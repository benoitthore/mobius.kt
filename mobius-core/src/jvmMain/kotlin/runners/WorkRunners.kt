package kt.mobius.runners

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

actual object WorkRunners {

    @JvmStatic
    actual fun immediate(): WorkRunner {
        return ImmediateWorkRunner()
    }

}
