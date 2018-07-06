/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.macros.derive.{ deriveObjectType, ObjectTypeDescription }
import scala.collection.JavaConverters._
import software.amazon.awssdk.services.ecs.model.Container

import social.midas.discovery.common.aws.{ Arn, ArnLike }

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
final case class EcsContainerArn(
  arn: Arn,
  clusterArn: EcsClusterArn,
) extends ArnLike

object EcsContainerArn {
  implicit val dec: Decoder[EcsContainerArn] = deriveDecoder[EcsContainerArn]
  implicit val enc: Encoder[EcsContainerArn] = deriveEncoder[EcsContainerArn]
}

final case class EcsContainer(
  arn: Arn,
  clusterArn: EcsClusterArn,
  networkBindings: Seq[EcsNetworkBinding],
)

object EcsContainer {
  implicit val dec: Decoder[EcsContainer] = deriveDecoder[EcsContainer]
  implicit val enc: Encoder[EcsContainer] = deriveEncoder[EcsContainer]
  implicit  val Type = deriveObjectType[Unit, EcsContainer](
    ObjectTypeDescription("An ECS container"),
  )

  def apply(clusterArn: String, container: Container): EcsContainer = {
    EcsContainer(
      Arn(container.containerArn()),
      EcsClusterArn(clusterArn),
      container.networkBindings.asScala.map(EcsNetworkBinding(_))
    )
  }
}
