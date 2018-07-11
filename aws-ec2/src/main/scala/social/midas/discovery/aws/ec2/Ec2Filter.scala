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
package social.midas.discovery.aws.ec2

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros.derive.deriveInputObjectType
import sangria.marshalling.circe._
import sangria.schema.{ Argument, ListInputType, OptionInputType }
import software.amazon.awssdk.services.ec2.model.{Filter => AwsFilter}

final case class Ec2Filter(
  name: String,
  values: Seq[String],
) {
  def toAws: AwsFilter =
    AwsFilter.builder()
      .name(name)
      .values(values: _*)
      .build()
}

object Ec2Filter {
  /** JSON decoder */
  implicit val dec: Decoder[Ec2Filter] = deriveDecoder[Ec2Filter]
  /** JSON encoder */
  implicit val enc: Encoder[Ec2Filter] = deriveEncoder[Ec2Filter]

  implicit val Type = deriveInputObjectType[Ec2Filter]()

  val Args = Argument(
    "filters",
    OptionInputType(ListInputType(Type)),
    description = "Provide filters on a EC2 query.",
  )
}
