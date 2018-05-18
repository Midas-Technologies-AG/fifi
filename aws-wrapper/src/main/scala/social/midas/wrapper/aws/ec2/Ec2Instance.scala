/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ec2

import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import software.amazon.awssdk.services.ec2.model.Instance

case class Ec2InstanceId(unwrap: String)

object Ec2InstanceId {
  implicit val enc: Encoder[Ec2InstanceId] = new Encoder[Ec2InstanceId] {
    final def apply(x: Ec2InstanceId): Json = Json.fromString(x.unwrap)
  }
  implicit val dec: Decoder[Ec2InstanceId] = new Decoder[Ec2InstanceId] {
    final def apply(c: HCursor): Decoder.Result[Ec2InstanceId] =
      c.as[String].map(x => Ec2InstanceId(x))
  }

}

case class Ec2Instance(
  id: Ec2InstanceId,
  privateIpAddress: String,
)

object Ec2Instance {
  def apply(instance: Instance): Ec2Instance =
    Ec2Instance(
      Ec2InstanceId(instance.instanceId()),
      instance.privateIpAddress(),
    )

  implicit val dec: Decoder[Ec2Instance] = deriveDecoder[Ec2Instance]
  implicit val enc: Encoder[Ec2Instance] = deriveEncoder[Ec2Instance]
}
