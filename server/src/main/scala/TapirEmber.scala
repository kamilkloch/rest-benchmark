import cats.effect.{IO, IOApp}
import config.{TapirConfig, WebServerConfig}

object TapirEmber extends IOApp.Simple {
  override protected def computeWorkerThreadCount: Int = WebServerConfig.mainPoolSize

  def run: IO[Unit] = WebServerConfig.ember.serverResource(TapirConfig.service).useForever
}
