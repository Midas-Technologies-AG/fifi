/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import software.amazon.awssdk.services.ecs.model.ContainerInstance

import social.midas.wrapper.aws.generic.{ Arn, ArnLike }
import social.midas.wrapper.aws.ec2.Ec2InstanceId

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
case class EcsContainerInstanceArn(
  arn: Arn,
  clusterArn: EcsClusterArn,
) extends ArnLike

object EcsContainerInstanceArn {

  /** JSON decoder. */
  implicit val dec: Decoder[EcsContainerInstanceArn] =
    deriveDecoder[EcsContainerInstanceArn]

  /** JSON encoder. */
  implicit val enc: Encoder[EcsContainerInstanceArn] =
    deriveEncoder[EcsContainerInstanceArn]

  /**
   * Wrap the supplied strings before construction.
   */
  def apply(arn: String, clusterArn: String): EcsContainerInstanceArn =
    EcsContainerInstanceArn(Arn(arn), EcsClusterArn(clusterArn))
}

case class EcsContainerInstance(
  arn: Arn,
  clusterArn: EcsClusterArn,
  ec2Instance: Ec2InstanceId,
) {
  def getArnPath = EcsContainerInstanceArn(arn, clusterArn)
}

object EcsContainerInstance {
  implicit val dec: Decoder[EcsContainerInstance] =
    deriveDecoder[EcsContainerInstance]
  implicit val enc: Encoder[EcsContainerInstance] =
    deriveEncoder[EcsContainerInstance]

  def apply(cluster: EcsClusterArn, instance: ContainerInstance)
      : EcsContainerInstance =
    EcsContainerInstance(
      Arn(instance.containerInstanceArn()),
      cluster,
      Ec2InstanceId(instance.ec2InstanceId()),
    )
}
