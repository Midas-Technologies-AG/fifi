/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ec2

import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import software.amazon.awssdk.services.ec2.model.Instance

/**
 * A thin wrapper around an instance id of an EC2 machine.
 * 
 * == JSON Deserialization ==
 * 
 * {{{
 * scala> import io.circe.literal._
 * scala> import social.midas.wrapper.aws.ec2.Ec2InstanceId
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[Ec2InstanceId]
 * res0: io.circe.Decoder.Result[Ec2InstanceId] = Right(Ec2InstanceId(foo))
 * }}}
 */
final case class Ec2InstanceId(unwrap: String)

object Ec2InstanceId {

  /**
   * JSON encoder which simply unwraps the string (thin wrapper).
   */
  implicit val enc: Encoder[Ec2InstanceId] = new Encoder[Ec2InstanceId] {
    def apply(x: Ec2InstanceId): Json = Json.fromString(x.unwrap)
  }

  /**
   * JSON decoder which simply wraps up a string (thin wrapper).
   */
  implicit val dec: Decoder[Ec2InstanceId] = new Decoder[Ec2InstanceId] {
    def apply(c: HCursor): Decoder.Result[Ec2InstanceId] =
      c.as[String].map(x => Ec2InstanceId(x))
  }

}

/**
 * The relevant parts of a description of an EC2 instance.
 */
final case class Ec2Instance(
  id: Ec2InstanceId,
  privateIpAddress: String,
)

object Ec2Instance {

  /**
   * Construct an [[Ec2Instance]] from a AWS API call result.
   */
  def apply(instance: Instance): Ec2Instance =
    Ec2Instance(
      Ec2InstanceId(instance.instanceId()),
      instance.privateIpAddress(),
    )

  /** JSON decoder */
  implicit val dec: Decoder[Ec2Instance] = deriveDecoder[Ec2Instance]
  /** JSON encoder */
  implicit val enc: Encoder[Ec2Instance] = deriveEncoder[Ec2Instance]
}
