package config

import cats.effect.*
import fs2.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.{CodecFormat, endpoint, stringBody, webSocketBody}

import scala.concurrent.duration.*

/** Config shared among blaze/ember tapir servers */
object TapirConfig {
  private val helloEndpoint = endpoint.get
    .in("hello")
    .out(stringBody)

  private val routes = Http4sServerInterpreter[IO]().toRoutes(helloEndpoint.serverLogicSuccess(_ => IO.pure("world")))

  def service: HttpApp[IO] = Router("/" -> routes).orNotFound
}
