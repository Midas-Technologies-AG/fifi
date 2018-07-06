/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common.aws

import io.circe.{ Decoder, Encoder, HCursor, Json }
import sangria.schema.{ Argument, OptionInputType, ScalarAlias, StringType }
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
 * scala> import social.midas.discovery.common.aws.Arn
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[Arn]
 * res0: io.circe.Decoder.Result[Arn] = Right(Arn(foo))
 * }}}
 */
final case class Arn(unwrap: String) extends ArnLike {
  def arn: Arn = this
}

object Arn {
  implicit val enc: Encoder[Arn] = new Encoder[Arn] {
    def apply(x: Arn): Json = Json.fromString(x.unwrap)
  }
  implicit val dec: Decoder[Arn] = new Decoder[Arn] {
    def apply(c: HCursor): Decoder.Result[Arn] =
      c.as[String].map(Arn(_))
  }

  implicit val Type = ScalarAlias[Arn, String](
    StringType, _.unwrap, x => Right(Arn(x)),
  )

  implicit val RegexType = ScalarAlias[Regex, String](
    StringType, _.regex, x => Right(new Regex(x)),
  )

  val Filter = Argument(
    "filterArn",
    OptionInputType(RegexType),
    description = "Filter ARNs by matching to a regular expression.",
  )
}
