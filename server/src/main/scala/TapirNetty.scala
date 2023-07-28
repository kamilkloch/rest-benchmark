import cats.effect.{IO, IOApp}
import config.TapirConfig
import config.WebServerConfig

object TapirNetty extends IOApp.Simple {
  override protected def computeWorkerThreadCount: Int = WebServerConfig.mainPoolSize

  def run: IO[Unit] = TapirConfig.netty.ce.serverResource.useForever
}
