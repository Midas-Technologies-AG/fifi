/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import scala.collection.JavaConverters._
import software.amazon.awssdk.services.ecs.model.Task

import social.midas.wrapper.aws.generic.{ Arn, ArnLike }

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
case class EcsTaskArn(
  arn: Arn,
  clusterArn: EcsClusterArn,
) extends ArnLike

object EcsTaskArn {
  implicit val dec: Decoder[EcsTaskArn] = deriveDecoder[EcsTaskArn]
  implicit val enc: Encoder[EcsTaskArn] = deriveEncoder[EcsTaskArn]
}

/**
 * The description of an ECS task.
 */
case class EcsTask(
  arn: Arn,
  clusterArn: EcsClusterArn,
  containerInstance: EcsContainerInstanceArn,
  containers: Seq[EcsContainer],
  group: String,
) extends ArnLike {
  def getFullArn = EcsTaskArn(arn, clusterArn)
}

object EcsTask {
  implicit val dec: Decoder[EcsTask] = deriveDecoder[EcsTask]
  implicit val enc: Encoder[EcsTask] = deriveEncoder[EcsTask]

  def apply(task: Task): EcsTask =
    EcsTask(
      Arn(task.taskArn()),
      EcsClusterArn(task.clusterArn()),
      EcsContainerInstanceArn(
        task.containerInstanceArn(),
        task.clusterArn(),
      ),
      containers = task.containers().asScala.toSeq.map(x => EcsContainer(task.clusterArn(), x)),
      group      = task.group(),
    )
}
