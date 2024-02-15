# Scala rest benchmark

## Test setup

Client and server run on two separate machines. Both share the same setup: 
 - Intel® Core™ i9-13900K CPU @ 3.0GHz (max 5.8GHz, performance-cores only), 
 - RAM 64GB DDR5-4800,
 - 10Gbit network,
 - Ubuntu 23.04 (Linux 6.6), 
 - Oracle JDK 21.

### Server

Server code resides in the `/server` module. Server exposes a single  GET `/ts` endpoint, 
which returns server Epoch clock.    

Tested servers:
 - [http4s] + blaze ([CE] 3.5.3, [fs2] 3.9.4)
 - [http4s-netty] ([CE] 3.5.3, [fs2] 3.9.4)
 - [http4s] + blaze, via [tapir] ([CE] 3.5.3, [fs2] 3.9.4) 
 - [netty], via [tapir] ([CE] 3.5.3, [fs2] 3.9.4) 
 - [zio-http] ([zio-http] 3.0.0-RC2, [zio] 2.0.21)
 - [netty], via [tapir] ([zio] 2.0.21)

### Client 

Client code resides in the `/client` module. [Gatling] client ramps up to 500 user connections within 30s,
and each user issues a `GET /ts` request at a rate 1K req/s.  
For each response, an absolute difference between the client timestamp and the timestamp received from the server
is stored into an [HdrHistogram]. With clocks synchronized between the client and server, this value corresponds
to the latency induced by the server.
 
### Clock synchronization

For precise measurement of latency up to milliseconds need to install, configure, and run `chrony` service.

The following command could be used for installation on Ubuntu:
```sh
sudo apt-get -y install chrony
```

Here is a list of NTP servers that is used in our `/etc/chrony/chrony.conf`:
```
        server time5.facebook.com iburst
       	server tempus1.gum.gov.pl
       	server tempus2.gum.gov.pl
        server ntp1.tp.pl
        server ntp2.tp.pl 
```

For non-Poland regions [other servers could be preferred](https://gist.github.com/mutin-sa/eea1c396b1e610a2da1e5550d94b0453).

Finally, need to restart the service after (re)configuration by:
```
sudo systemctl restart chrony
```

[Here](https://engineering.fb.com/2020/03/18/production-engineering/ntp-service/) is a great article about time synchronization in Facebook.

## Benchmarks

Benchmark results reside in `/results`.
```
 results
 ├── http4s-blaze      (CE 3.5.3, fs2 3.9.4)
 ├── http4s-netty      (CE 3.5.3, fs2 3.9.4)
 ├── tapir-blaze       (CE 3.5.3, fs2 3.9.4, tapir 1.9.9, server log off)
 ├── tapir-blaze-log   (CE 3.5.3, fs2 3.9.4, tapir 1.9.9, server log on)
 ├── tapir-netty       (CE 3.5.3, fs2 3.9.4, tapir 1.9.9)
 ├── zio-http          (zio-http 3.0.0-RC2, zio 2.0.21)
 └── zio-tapir-netty   (tapir 1.9.9)
```

Each folder contains:
- [HdrHistogram] latency,
- [Gatling] html report,
- [async-profiler] flame graphs in 2 flavours: per-thread and aggregated.

Quick summary:

![rest-benchmark-500-1k](results/rest-benchmark-500-1k.png)

## How to run benchmarks

Note: you need Java 21.0.1+ to build and run the benchmarks.

1. Build server binaries via
   ```bash
   sbt stage
   ```
2. Start the desired server using binaries found in `server/target/universal/stage/bin`
3. Start [gatling] web socket client via
   ```bash
    sbt client/Gatling/test
   ```

## Acknowledgements

The majority of the work behind the tests is carried out by [Andriy Plokhotnyuk](https://github.com/plokhotnyuk).
Thank, you Andriy!

[tapir]: https://github.com/softwaremill/tapir
[gatling]: https://github.com/gatling/gatling
[http4s]: https://github.com/http4s/http4s
[http4s-netty]: https://github.com/http4s/http4s-netty
[netty]: https://github.com/netty/netty
[zio-http]: https://github.com/zio/zio-http
[zio]: https://github.com/zio/zio
[CE]: https://github.com/typelevel/cats-effect
[fs2]: https://github.com/typelevel/fs2
[HdrHistogram]: https://github.com/HdrHistogram/HdrHistogram
[async-profiler]: https://github.com/async-profiler/async-profiler
