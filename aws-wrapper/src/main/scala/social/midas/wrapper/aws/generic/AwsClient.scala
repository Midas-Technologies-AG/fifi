/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.generic

import cats.effect.IO
import java.util.concurrent.{
 CancellationException, CompletableFuture, CompletionException,
}
import java.util.function.BiFunction
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.utils.SdkAutoCloseable

/**
 * Generic abstraction of AWS an async client of type `C` with a
 * builder `B`.
 */
abstract class AwsClient[
  B <: AwsClientBuilder[B,C],
  C <: SdkAutoCloseable,
] {

  def region: Region
  protected def builder: B

  private lazy val client: C = builder.region(region).build()

  /**
   * Use this to do AWS SDK calls on this client.
   * 
   * It wraps the call into an [[cats.effect.IO]] which is cancelable.
   */
  def onClient[A](f: C => CompletableFuture[A]): IO[A] =
    IO.cancelable(cb => {
      val handler = new BiFunction[A, Throwable, Unit] {
        override def apply(result: A, err: Throwable): Unit = {
          err match {
            case null =>
              cb(Right(result))
            case _: CancellationException =>
              ()
            case ex: CompletionException if ex.getCause().ne(null) =>
              cb(Left(ex.getCause()))
            case ex =>
              cb(Left(ex))
          }
        }
      }

      val cf = f(client)
      cf.handle[Unit](handler)
      IO(cf.cancel(true))
    })

  /**
   * Close the underlying client.
   */
  def close(): IO[Unit] = IO(client.close())
}
