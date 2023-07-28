package config

import cats.effect.*
import cats.effect.std.Dispatcher
import config.WebServerConfig.{connectorPoolSize, host, port}
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.netty.NettyConfig.EventLoopConfig
import sttp.tapir.server.netty.cats.{NettyCatsServer, NettyCatsServerBinding, NettyCatsServerOptions}
import sttp.tapir.server.netty.zio.{NettyZioServer, NettyZioServerBinding, NettyZioServerOptions}
import sttp.tapir.server.netty.{NettyConfig, NettySocketConfig}
import sttp.tapir.ztapir.ZServerEndpoint
import sttp.tapir.{endpoint, stringBody}
import zio.Clock.ClockLive
import zio.{Console, ZIO}

import java.util.concurrent.TimeUnit

/** Config shared among blaze/ember tapir CE/ZIO servers */
object TapirConfig {
  private val tsEndpoint = endpoint.get
    .in("ts")
    .out(stringBody)
  private val tsServerEndpoint = tsEndpoint.serverLogicSuccess(_ => IO.realTime.map(_.toMillis.toString))
  private val zioTsServerEndpoint: ZServerEndpoint[Any, Any] =
    tsEndpoint.serverLogicSuccess(_ => ClockLive.currentTime(TimeUnit.MILLISECONDS).map(_.toString))
  private val serverOptions = Http4sServerOptions
    .customiseInterceptors[IO]
    .serverLog(None)
    .options
  private val routes = Http4sServerInterpreter[IO](serverOptions)
    .toRoutes(tsServerEndpoint)

  def service: HttpApp[IO] = Router("/" -> routes).orNotFound

  object netty {
    private def nettyConfig(connectorPoolSize: Int) = NettyConfig
      .defaultNoStreaming
      .host(host.toString)
      .port(port.value)
      .eventLoopConfig(EventLoopConfig(() => new EpollEventLoopGroup(connectorPoolSize), classOf[EpollServerSocketChannel]))
      .socketConfig(NettySocketConfig.default.withTcpNoDelay.withReuseAddress)

    object ce {
      val serverResource: Resource[IO, NettyCatsServerBinding[IO]] = {
        Dispatcher.parallel[IO].evalMap { dispatcher =>
          val nettyCatsServerOptions = NettyCatsServerOptions
            .customiseInterceptors(dispatcher)
            .serverLog(None)
            .options

          NettyCatsServer(nettyCatsServerOptions, nettyConfig(connectorPoolSize))
            .addEndpoint(tsServerEndpoint)
            .start()
            .flatTap(binding => IO.println(s"Netty server started on port ${binding.port}"))
        }
      }
    }

    object zio {
      private val nettyZioServerOptions = NettyZioServerOptions
        .customiseInterceptors[Any]
        .serverLog(None)
        .options

      val server: ZIO[Any, Throwable, NettyZioServerBinding[Any]] =
        NettyZioServer(nettyZioServerOptions, nettyConfig(connectorPoolSize * 3))
          .addEndpoint(zioTsServerEndpoint)
          .start()
          .tap(binding => Console.printLine(s"Netty server started on port ${binding.port}"))
    }
  }
}
