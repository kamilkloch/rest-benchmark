import cats.effect.*
import config.WebServerConfig

object Http4sNetty extends IOApp.Simple {

  override protected def computeWorkerThreadCount: Int = Math.max(2, super.computeWorkerThreadCount / 2)

  def run: IO[Unit] = WebServerConfig.netty.serverResource(WebServerConfig.service).useForever
}
