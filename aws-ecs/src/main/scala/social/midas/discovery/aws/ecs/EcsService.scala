/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros.derive.{ deriveObjectType, ObjectTypeDescription }

import social.midas.discovery.common.aws.{ Arn, ArnLike }

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
final case class EcsServiceArn(
  arn: Arn,
  clusterArn: EcsClusterArn,
) extends ArnLike

object EcsServiceArn {
  implicit val dec: Decoder[EcsServiceArn] = deriveDecoder[EcsServiceArn]
  implicit val enc: Encoder[EcsServiceArn] = deriveEncoder[EcsServiceArn]
  implicit val Type = deriveObjectType[Unit, EcsServiceArn](
    ObjectTypeDescription("An ECS service"),
  )
}
