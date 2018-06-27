/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.generic

import io.circe.{ Decoder, Encoder, HCursor, Json }
import scala.util.matching.Regex

/**
 * Define basic operations we need on all ARN-like objects.
 */
trait ArnLike {

  /**
   * The ARN of this object.
   */
  def arn: Arn

  /**
   * Test wether the ARN matches the regular expression `regex`.
   */
  def arnMatches(regex: Regex): Boolean = {
    arn.unwrap match {
      case regex(_*) => true
      case _ => false
    }
  }
}

/**
 * A thin wrapper around an ARN (Amazon Resource Name).
 * 
 * == JSON Deserialization ==
 * 
 * {{{
 * scala> import io.circe.literal._
 * scala> import social.midas.wrapper.aws.generic.Arn
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[Arn]
 * res0: io.circe.Decoder.Result[Arn] = Right(Arn(foo))
 * }}}
 */
sealed case class Arn(unwrap: String) extends ArnLike {
  def arn = this
}

object Arn {
  implicit val enc: Encoder[Arn] = new Encoder[Arn] {
    final def apply(x: Arn): Json = Json.fromString(x.unwrap)
  }
  implicit val dec: Decoder[Arn] = new Decoder[Arn] {
    final def apply(c: HCursor): Decoder.Result[Arn] =
      c.as[String].map(Arn(_))
  }
}
