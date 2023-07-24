# Scala rest benchmark

## Test setup

Client and server run on two separate machines. Both share the same setup: 
 - Intel® Core™ i9-13900K CPU @ 3.0GHz (max 5.8GHz, performance-cores only), 
 - RAM 64GB DDR5-4800,
 - 10Gbit network,
 - Ubuntu 23.04 (Linux 6.2), 
 - Oracle GraalVM 23.0 for Java 20.

### Server

Server code resides in the `/server` module. Server exposes a single  GET `/hello` endpoint  

Tested servers:
 - [http4s] + blaze ([CE] 3.5.1, [fs2] 3.7.0)
 - [http4s] + ember ([CE] 3.5.1, [fs2] 3.7.0)
 - [http4s] + netty ([CE] 3.5.1, [fs2] 3.7.0)
 - [http4s] + blaze, via [tapir] ([CE] 3.5.1, [fs2] 3.7.0) 
 - [http4s] + ember, via [tapir] ([CE] 3.5.1, [fs2] 3.7.0)
 - [http4s] + netty, via [tapir] ([CE] 3.5.1, [fs2] 3.7.0)
 - [zio-http] ([zio-http] 3.0.0-RC2, [zio] 2.0.15)
 
### Client 

Client code resides in the `/client` module. 


## Benchmarks
TODO

## Acknowledgements

The majority of the work behind the tests is carried out by [Andriy Plokhotnyuk](https://github.com/plokhotnyuk).
Thank, you Andriy!

[tapir]: https://github.com/softwaremill/tapir
[gatling]: https://github.com/gatling/gatling
[babl]: https://github.com/babl-ws/babl
[http4s]: https://github.com/http4s/http4s
[zio-http]: https://github.com/zio/zio-http
[zio]: https://github.com/zio/zio
[CE]: https://github.com/typelevel/cats-effect
[fs2]: https://github.com/typelevel/fs2
[HdrHistogram]: https://github.com/HdrHistogram/HdrHistogram
[async-profiler]: https://github.com/async-profiler/async-profiler
