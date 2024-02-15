val http4sVersion = "0.23.25"
val blazeVersion = "0.23.16"
val tapirVersion = "1.9.9"
val http4sNettyVersion = "0.5.12"
val catsEffectVersion = "3.5.3"
val fs2Version = "3.9.4"
val zioHttpVersion = "3.0.0-RC2"
val zioVersion = "2.0.21"
val gatlingVersion = "3.10.3"
val logbackVersion = "1.4.14"

// compiler options explicitly disabled from https://github.com/DavidGregory084/sbt-tpolecat
val disabledScalacOptionsCompile = Set(
  "-Xfatal-warnings",
  "-Wunused:privates",
)

lazy val commonSettings = Def.settings(
  name := "rest-benchmark",
  version := "0.1.0-SNAPSHOT",
  fork := true,
  scalaVersion := "3.3.1",
  scalacOptions ++= Seq("-release", "21"),
  javacOptions ++= Seq("-source", "21", "-target", "21"),
  Compile / scalacOptions ~= ((options: Seq[String]) => options.filterNot(disabledScalacOptionsCompile)),
  Compile / scalacOptions ++= Seq(
    "-Wnonunit-statement",
  ),
  javaOptions := Seq(
    "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens", "java.base/java.util.zip=ALL-UNNAMED",
    "-Dcats.effect.tracing.mode=none",
    "-Dcats.effect.tracing.exceptions.enhanced=false",
    "-Dcats.effect.tracing.buffer.size=64",
    "-Djava.lang.Integer.IntegerCache.high=65536",
    "-Djava.net.preferIPv4Stack=true",
    "-XX:+UnlockExperimentalVMOptions",
    "-XX:+TrustFinalNonStaticFields",
    "-Xms32g",
    "-Xmx32g",
    "-XX:+AlwaysPreTouch",
    "-XX:+UseZGC",
    "-XX:+ZGenerational",
    "-XX:TLABSize=1m",
    "-XX:-ResizeTLAB",
    "-XX:InitialCodeCacheSize=256m",
    "-XX:ReservedCodeCacheSize=256m",
    "-XX:NonNMethodCodeHeapSize=16m",
    "-XX:NonProfiledCodeHeapSize=120m",
    "-XX:ProfiledCodeHeapSize=120m",
  ),
)

lazy val server = (project in file("server"))
  .settings(name := "server")
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % blazeVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-netty-server" % http4sNettyVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-cats" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-zio" % tapirVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-http" % zioHttpVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
    )
  )
  .settings(
    Universal / javaOptions := Seq(
      "-J--add-opens",
      "-Jjava.base/sun.nio.ch=ALL-UNNAMED",
      "-J--add-opens",
      "-Jjava.base/java.util.zip=ALL-UNNAMED",
      "-J-Dcats.effect.tracing.mode=none",
      "-J-Dcats.effect.tracing.exceptions.enhanced=false",
      "-J-Dcats.effect.tracing.buffer.size=64",
      "-J-Djava.lang.Integer.IntegerCache.high=65536",
      "-J-Djava.net.preferIPv4Stack=true",
      "-J-XX:+UnlockExperimentalVMOptions",
      "-J-XX:+TrustFinalNonStaticFields",
      "-J-Xms32g",
      "-J-Xmx32g",
      "-J-XX:+AlwaysPreTouch",
      "-J-XX:+UseZGC",
      "-J-XX:+ZGenerational",
      "-J-XX:TLABSize=1m",
      "-J-XX:-ResizeTLAB",
      "-J-XX:InitialCodeCacheSize=256m",
      "-J-XX:ReservedCodeCacheSize=256m",
      "-J-XX:NonNMethodCodeHeapSize=16m",
      "-J-XX:NonProfiledCodeHeapSize=120m",
      "-J-XX:ProfiledCodeHeapSize=120m",
    )
  )

lazy val client = (project in file("client"))
  .settings(name := "client")
  .enablePlugins(GatlingPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion % Test,
      "io.gatling" % "gatling-test-framework" % gatlingVersion % Test,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
    ),
    Gatling / javaOptions := overrideDefaultJavaOptions(
      "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens", "java.base/java.util.zip=ALL-UNNAMED",
      "-Djava.lang.Integer.IntegerCache.high=65536",
      "-Djava.net.preferIPv4Stack=true",
      "-XX:+UnlockExperimentalVMOptions",
      "-XX:+TrustFinalNonStaticFields",
      "-Xms32g",
      "-Xmx32g",
      "-XX:+AlwaysPreTouch",
      "-XX:+UseZGC",
      "-XX:+ZGenerational",
      "-XX:TLABSize=1m",
      "-XX:-ResizeTLAB",
      "-XX:InitialCodeCacheSize=256m",
      "-XX:ReservedCodeCacheSize=256m",
      "-XX:NonNMethodCodeHeapSize=16m",
      "-XX:NonProfiledCodeHeapSize=120m",
      "-XX:ProfiledCodeHeapSize=120m",
    ),
  )

lazy val rest_benchmark = (project in file("."))
  .settings(name := "rest-benchmark")
  .disablePlugins(JavaAppPackaging)
  .aggregate(server)
  .aggregate(client)
