package config

import cats.effect.*
import com.comcast.ip4s.{Hostname, IpLiteralSyntax, Port}
import fs2.*
import org.http4s.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.Server
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.duration.*

/** Config shared among blaze/ember/zio-http http4s/tapir servers */
object WebServerConfig {

  val port: Port = port"8888"
  val host: Hostname = host"0.0.0.0"
  val connectorPoolSize: Int = Math.max(2, Runtime.getRuntime.availableProcessors() / 4)
  private val maxConnections: Int = 65536

  val service: HttpApp[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl.*

    HttpRoutes
      .of[IO] {
        case GET -> Root / "hello" => Ok("world")
      }
      .orNotFound
  }

  object blaze {
    def serverResource(httpApp: HttpApp[IO]): Resource[IO, Server] = {
      BlazeServerBuilder[IO]
        .bindHttp(port.value, host.toString)
        .withMaxConnections(maxConnections)
        .withConnectorPoolSize(connectorPoolSize)
        .withHttpApp(httpApp)
        .resource
    }
  }

  object ember {
    private val idleTimeout: FiniteDuration = 1.hour
    private val shutdownTimeout: FiniteDuration = 1.hour
    private val requestHeaderReceiveTimeout: FiniteDuration = 1.hour

    def serverResource(httpApp: HttpApp[IO]): Resource[IO, Server] = {
      EmberServerBuilder.default[IO]
        .withPort(port)
        .withHost(host)
        .withMaxConnections(maxConnections)
        .withHttpApp(httpApp)
        .withIdleTimeout(idleTimeout)
        .withShutdownTimeout(shutdownTimeout)
        .withRequestHeaderReceiveTimeout(requestHeaderReceiveTimeout)
        .build
    }
  }

  object netty {

  }
}
