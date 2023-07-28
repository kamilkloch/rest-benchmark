import cats.effect.*
import config.WebServerConfig

object Http4sNetty extends IOApp.Simple {
  override protected def computeWorkerThreadCount: Int = WebServerConfig.mainPoolSize

  def run: IO[Unit] = WebServerConfig.netty.serverResource(WebServerConfig.service).useForever
}
