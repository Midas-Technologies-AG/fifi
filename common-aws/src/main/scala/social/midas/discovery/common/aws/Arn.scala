/**
 * Copyright 2018 Midas Technologies AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
