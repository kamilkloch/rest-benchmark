package gatling

import io.gatling.core.Predef.*
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import org.HdrHistogram.ConcurrentHistogram

import scala.concurrent.duration.DurationInt

class SimpleRestServerSimulation extends Simulation {

  import SimpleRestServerSimulation.*

  private val restHttpProtocol = http
    .baseUrl(config.restServerUri)
    .disableUrlEncoding
    .disableCaching

  def helloRequests(name: String): ChainBuilder = during(60.seconds)(pace(1.millis).exec(
    http(name).get("/ts").check(
      bodyString.transform { ts =>
        hist.recordValue(Math.max(System.currentTimeMillis() - ts.toLong, 0))
      }
    )
  ))

  private val warmup = scenario("REST warmup")
    .exec(helloRequests("GET /ts warmup"))
    .exec(pause(6.seconds)) // waiting for closing of all connections before measurement
    .inject(config.injectionPolicy)

  private val measurement = scenario("REST measurement")
    .exec(helloRequests("GET /ts measurement"))
    .inject(config.injectionPolicy)

  setUp(
    warmup.andThen(measurement)
  ).protocols(restHttpProtocol)
}

object SimpleRestServerSimulation {
  private val hist = new ConcurrentHistogram(1L, 10000L, 3)

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit =
      hist.outputPercentileDistribution(System.out, 1.0)
  })

  object config {
    val numberOfUsers = 250

    val restServerUri = "http://127.0.0.1:8888"

    val injectionPolicy: OpenInjectionStep = rampUsers(numberOfUsers).during(30.seconds)
  }
}
