package config

import cats.effect.*
import com.comcast.ip4s.*
import fs2.io.net.SocketOption
import io.netty.channel.ChannelOption
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.netty.{NettyChannelOptions, NettyTransport}
import org.http4s.netty.server.NettyServerBuilder
import org.http4s.server.Server

import java.net.StandardSocketOptions
import scala.concurrent.duration.*

/** Config shared among blaze/ember/zio-http http4s/tapir servers */
object WebServerConfig {
  val port: Port = port"8888"
  val host: Hostname = host"0.0.0.0"
  val mainPoolSize: Int = Math.max(2, Runtime.getRuntime.availableProcessors() / 2)
  val connectorPoolSize: Int = Math.max(2, Runtime.getRuntime.availableProcessors() / 4)
  private val maxConnections: Int = 65536

  val service: HttpApp[IO] = {
    val dsl = new Http4sDsl[IO] {}

    import dsl.*

    HttpRoutes
      .of[IO] {
        case GET -> Root / "ts" => IO.realTime.flatMap(t => Ok(t.toMillis.toString))
      }
      .orNotFound
  }

  object netty {
    def serverResource(httpApp: HttpApp[IO]): Resource[IO, Server] = {
      NettyServerBuilder[IO]
        .bindHttp(port.value, host.toString)
        .withoutSsl
        .withHttpApp(httpApp)
        .withTransport(NettyTransport.Epoll)
        .withEventLoopThreads(connectorPoolSize)
        .withNettyChannelOptions(NettyChannelOptions.empty
            .append(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)
            .append(ChannelOption.SO_REUSEADDR, java.lang.Boolean.TRUE))
        .resource
    }
  }

  object blaze {
    def serverResource(httpApp: HttpApp[IO]): Resource[IO, Server] = {
      BlazeServerBuilder[IO]
        .bindHttp(port.value, host.toString)
        .withMaxConnections(maxConnections)
        .withConnectorPoolSize(connectorPoolSize)
        .withHttpApp(httpApp)
        .withDefaultTcpNoDelay
        .withDefaultSocketReuseAddress
        .resource
    }
  }

  object ember {
    def serverResource(httpApp: HttpApp[IO]): Resource[IO, Server] = {
      EmberServerBuilder.default[IO]
        .withPort(port)
        .withHost(host)
        .withMaxConnections(maxConnections)
        .withHttpApp(httpApp)
        .withIdleTimeout(1.hour)
        .withAdditionalSocketOptions(List(
          SocketOption(StandardSocketOptions.TCP_NODELAY, java.lang.Boolean.TRUE),
          SocketOption(StandardSocketOptions.SO_REUSEADDR, java.lang.Boolean.TRUE)))
        .build
    }
  }
}
