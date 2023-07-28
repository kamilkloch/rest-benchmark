import cats.effect.{IO, IOApp}
import config.{TapirConfig, WebServerConfig}

object TapirBlaze extends IOApp.Simple {
  override protected def computeWorkerThreadCount: Int = WebServerConfig.mainPoolSize

  def run: IO[Unit] = WebServerConfig.blaze.serverResource(TapirConfig.service).useForever
}
