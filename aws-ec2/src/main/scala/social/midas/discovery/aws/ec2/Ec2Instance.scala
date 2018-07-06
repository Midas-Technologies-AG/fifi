/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws.ec2

import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros.derive.{
  deriveObjectType, ObjectTypeDescription, ReplaceField,
}
import sangria.schema.{
  fields, DeferredValue, Field, ObjectType, OptionType, StringType,
}
// TODO: check how to remove this import:
import scala.concurrent.ExecutionContext.Implicits.global
import software.amazon.awssdk.services.ec2.model.Instance

import social.midas.discovery.common.{ AbstractContext, Provides }

/**
 * A thin wrapper around an instance id of an EC2 machine.
 * 
 * == JSON Deserialization ==
 * 
 * {{{
 * scala> import io.circe.literal._
 * scala> import social.midas.discovery.aws.ec2.Ec2InstanceId
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

  implicit val Type = ObjectType(
    "Ec2Instance",
    "Describes an EC2 instance.",
    fields[AbstractContext, Ec2InstanceId](
      Field("id", StringType, resolve = _.value.unwrap),
      Field("privateIpAddress", OptionType(StringType),
        tags = Provides("ip4") :: Nil,
        resolve = ctx => DeferredValue(
          Fetchers.ec2DescribeInstances.defer(ctx.value),
        ).map(_.privateIpAddress),
      ),
    ),
  )

  
}

/**
 * The relevant parts of a description of an EC2 instance.
 */
final case class Ec2Instance(
  id: Ec2InstanceId,
  privateIpAddress: Option[String] = None,
)

object Ec2Instance {

  /**
   * Construct an [[Ec2Instance]] from a AWS API call result.
   */
  def apply(instance: Instance): Ec2Instance =
    Ec2Instance(
      Ec2InstanceId(instance.instanceId()),
      Option(instance.privateIpAddress()),
    )

  /** JSON decoder */
  implicit val dec: Decoder[Ec2Instance] = deriveDecoder[Ec2Instance]
  /** JSON encoder */
  implicit val enc: Encoder[Ec2Instance] = deriveEncoder[Ec2Instance]

  implicit val Type = deriveObjectType[Unit, Ec2Instance](
    ObjectTypeDescription("Describes an EC2 instance."),
    ReplaceField("id", Field("id", StringType, resolve = _.value.id.unwrap)),
  )
}
