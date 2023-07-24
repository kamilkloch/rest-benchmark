import config.WebServerConfig
import zio.*
import zio.http.*
import zio.http.netty.{ChannelType, NettyConfig}

object ZioHttp extends ZIOAppDefault {
  private val app: Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      case Method.GET -> Root / "hello" => Response.text("world")
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