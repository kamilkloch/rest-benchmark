import config.TapirConfig
import zio.*

object ZioTapirNetty extends ZIOAppDefault {
  override val run: ZIO[Any, Throwable, Nothing] =
    TapirConfig.netty.zio.server *> ZIO.never
}