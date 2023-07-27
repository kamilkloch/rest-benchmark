import cats.effect.{IO, IOApp}
import config.TapirConfig

object TapirNetty extends IOApp.Simple {

  override protected def computeWorkerThreadCount: Int = Math.max(2, super.computeWorkerThreadCount / 2)

  def run: IO[Unit] = TapirConfig.netty.ce.serverResource.useForever
}
