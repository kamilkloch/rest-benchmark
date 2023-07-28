import config.WebServerConfig
import zio.*
import zio.Clock.ClockLive
import zio.http.*
import zio.http.netty.{ChannelType, NettyConfig}

import java.util.concurrent.{ForkJoinPool, TimeUnit}

object ZioHttp extends ZIOAppDefault {
  private val app: Http[Any, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "ts" => ClockLive.currentTime(TimeUnit.MILLISECONDS).map(t => Response.text(t.toString))
    }
  private val serverConfig =
    Server.Config.default.binding(WebServerConfig.host.toString, WebServerConfig.port.value)
  private val nettyConfig = NettyConfig.default
    .leakDetection(NettyConfig.LeakDetectionLevel.DISABLED)
    .channelType(ChannelType.EPOLL)
    .maxThreads(WebServerConfig.connectorPoolSize)
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setExecutor(Executor.fromJavaExecutor(new ForkJoinPool(WebServerConfig.mainPoolSize,
      ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true)))
  override val run: ZIO[Any, Throwable, Nothing] = (Server.install(app).flatMap { port =>
    Console.printLine(s"Started server on port: $port")
  } *> ZIO.never)
    .provide(ZLayer.succeed(serverConfig), ZLayer.succeed(nettyConfig), Server.customized)
}