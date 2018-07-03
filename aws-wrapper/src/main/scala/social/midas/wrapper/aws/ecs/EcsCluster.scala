/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

import social.midas.wrapper.aws.generic.{ Arn, ArnLike }

/**
 * A thin wrapper around an ARN of an ECS cluster. 
 * 
 * == JSON Deserialization ==
 * 
 * {{{
 * scala> import io.circe.literal._
 * scala> import social.midas.wrapper.aws.ecs.EcsClusterArn
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[EcsClusterArn]
 * res0: io.circe.Decoder.Result[EcsClusterArn] = Right(EcsClusterArn(Arn(foo)))
 * }}}
 */
final case class EcsClusterArn(arn: Arn) extends ArnLike

object EcsClusterArn {
  def apply(arn: String): EcsClusterArn = EcsClusterArn(Arn(arn))

  /** JSON decoder. */
  implicit val dec: Decoder[EcsClusterArn] = new Decoder[EcsClusterArn] {
    def apply(c: HCursor): Decoder.Result[EcsClusterArn] =
      c.as[String].map(x => EcsClusterArn(Arn(x)))
  }

  /** JSON encoder. */
  implicit val enc: Encoder[EcsClusterArn] = new Encoder[EcsClusterArn] {
    def apply(x: EcsClusterArn): Json = Json.fromString(x.arn.unwrap)
  }
}

/**
 * Product object for ECS clusters and related information.
 */
final case class EcsCluster(
  arn: EcsClusterArn,
  containerInstances: Option[Seq[EcsContainerInstanceArn]] = None,
  services: Option[Seq[EcsServiceArn]] = None,
  tasks: Option[Seq[EcsTaskArn]] = None,
)

object EcsCluster {
  /** JSON encoder. */
  implicit val dec: Decoder[EcsCluster] = deriveDecoder[EcsCluster]

  /** JSON decoder. */
  implicit val enc: Encoder[EcsCluster] = deriveEncoder[EcsCluster]
}
