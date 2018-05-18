/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

import social.midas.wrapper.aws.generic.{ Arn, ArnLike }

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
case class EcsServiceArn(
  arn: Arn,
  clusterArn: EcsClusterArn,
) extends ArnLike

object EcsServiceArn {
  implicit val dec: Decoder[EcsServiceArn] = deriveDecoder[EcsServiceArn]
  implicit val enc: Encoder[EcsServiceArn] = deriveEncoder[EcsServiceArn]
}
