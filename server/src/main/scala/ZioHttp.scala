import config.WebServerConfig
import zio.*
import zio.Clock.ClockLive
import zio.http.*
import zio.http.netty.{ChannelType, NettyConfig}

import java.util.concurrent.TimeUnit

object ZioHttp extends ZIOAppDefault {
  private val app: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "ts" => ClockLive.currentTime(TimeUnit.MILLISECONDS).map(t => Response.text(t.toString))
    }

  private val config = Server.Config.default.binding(WebServerConfig.host.toString, WebServerConfig.port.value)

  private val configLayer = ZLayer.succeed(config)

  private val nettyConfig = NettyConfig.default
    .leakDetection(NettyConfig.LeakDetectionLevel.DISABLED)
    .channelType(ChannelType.NIO)
    .maxThreads(WebServerConfig.connectorPoolSize)

  private val nettyConfigLayer = ZLayer.succeed(nettyConfig)

  override val run: ZIO[Any, Throwable, Nothing] = (Server.install(app).flatMap { port =>
    Console.printLine(s"Started server on port: $port")
  } *> ZIO.never)
    .provide(configLayer, nettyConfigLayer, Server.customized)
}