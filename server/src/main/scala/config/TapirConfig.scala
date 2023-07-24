package config

import cats.effect.*
import config.WebServerConfig.{host, port}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.netty.NettyConfig
import sttp.tapir.server.netty.cats.{NettyCatsServer, NettyCatsServerBinding}
import sttp.tapir.{endpoint, stringBody}

/** Config shared among blaze/ember tapir servers */
object TapirConfig {
  private val tsServerEndpoint = endpoint.get
    .in("ts")
    .out(stringBody)
    .serverLogicSuccess(_ => IO.realTime.map(_.toMillis.toString))

  private val routes = Http4sServerInterpreter[IO]().toRoutes(tsServerEndpoint)

  def service: HttpApp[IO] = Router("/" -> routes).orNotFound

  object netty {
    private val nettyConfig = NettyConfig
      .defaultNoStreaming
      .host(host.toString)
      .port(port.value)

    val serverResource: Resource[IO, NettyCatsServerBinding[IO]] =
      NettyCatsServer.io(nettyConfig).evalMap { server =>
        server
          .addEndpoint(tsServerEndpoint)
          .start()
          .flatTap(binding => IO.println(s"Netty server started on port ${binding.port}"))
      }
  }
}
