/**
 * Copyright 2018 Midas Technologies AG
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
