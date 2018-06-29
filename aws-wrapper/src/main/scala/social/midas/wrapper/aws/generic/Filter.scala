/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.generic

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import software.amazon.awssdk.services.ec2.model.{Filter => AwsFilter}

final case class Filter(
  name: String,
  values: Seq[String],
) {
  def toAws: AwsFilter =
    AwsFilter.builder()
      .name(name)
      .values(values: _*)
      .build()
}

object Filter {
  /** JSON decoder */
  implicit val dec: Decoder[Filter] = deriveDecoder[Filter]
  /** JSON encoder */
  implicit val enc: Encoder[Filter] = deriveEncoder[Filter]
}
