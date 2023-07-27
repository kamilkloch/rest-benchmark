import config.TapirConfig
import zio.*

import java.util.concurrent.ForkJoinPool

object ZioTapirNetty extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setExecutor(Executor.fromJavaExecutor(new ForkJoinPool(
      Math.max(2, java.lang.Runtime.getRuntime.availableProcessors() / 2),
      ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)))

  override val run: ZIO[Any, Throwable, Nothing] =
    TapirConfig.netty.zio.server *> ZIO.never
}