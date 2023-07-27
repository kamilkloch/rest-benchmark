package config

import cats.effect.*
import cats.effect.std.Dispatcher
import config.WebServerConfig.{host, port}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.netty.cats.{NettyCatsServer, NettyCatsServerBinding, NettyCatsServerOptions}
import sttp.tapir.server.netty.{NettyConfig, NettySocketConfig}
import sttp.tapir.{endpoint, stringBody}

/** Config shared among blaze/ember tapir servers */
object TapirConfig {
  private val tsServerEndpoint = endpoint.get
    .in("ts")
    .out(stringBody)
    .serverLogicSuccess(_ => IO.realTime.map(_.toMillis.toString))

  private val serverOptions = Http4sServerOptions
    .customiseInterceptors[IO]
    .serverLog(None)
    .options

  private val routes = Http4sServerInterpreter[IO](serverOptions)
    .toRoutes(tsServerEndpoint)

  def service: HttpApp[IO] = Router("/" -> routes).orNotFound

  object netty {
    private val nettyConfig = NettyConfig
      .defaultNoStreaming
      .host(host.toString)
      .port(port.value)
      .socketConfig(NettySocketConfig.default.withTcpNoDelay.withReuseAddress)

    val serverResource: Resource[IO, NettyCatsServerBinding[IO]] = {
      Dispatcher.parallel[IO].evalMap { dispatcher =>
        val nettyCatsServerOptions = NettyCatsServerOptions
          .customiseInterceptors(dispatcher)
          .serverLog(None)
          .options

        NettyCatsServer(nettyCatsServerOptions, nettyConfig)
          .addEndpoint(tsServerEndpoint)
          .start()
          .flatTap(binding => IO.println(s"Netty server started on port ${binding.port}"))
      }
    }
  }
}
