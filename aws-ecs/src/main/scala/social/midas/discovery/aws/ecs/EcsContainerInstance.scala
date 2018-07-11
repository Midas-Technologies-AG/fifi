/**
 * Copyright 2018 Midas Technologies AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package social.midas.discovery.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.schema.{
  fields, DeferredValue, Field, ObjectType,
}
// TODO How can we eliminate this:
import scala.concurrent.ExecutionContext.Implicits.global
import software.amazon.awssdk.services.ecs.model.ContainerInstance

import social.midas.discovery.common.AbstractContext
import social.midas.discovery.common.aws.{ Arn, ArnLike }
import social.midas.discovery.aws.ec2.Ec2InstanceId

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
final case class EcsContainerInstanceArn(
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

  implicit val Type = ObjectType(
    "EcsContainerInstance",
    "An EC2 instance that is running the Amazon ECS agent and has been registered with a cluster.",
    fields[AbstractContext, EcsContainerInstanceArn](
      Field("arn", Arn.Type, resolve = _.value.arn),
      Field("clusterArn", EcsClusterArn.Type, resolve = _.value.clusterArn),
      Field("ec2Instance", Ec2InstanceId.Type,
        resolve = ctx => DeferredValue(
          Fetchers.ecsContainerInstances.defer(ctx.value)
        ).map(_.ec2Instance),
      ),
    ),
  )

  /**
   * Wrap the supplied strings before construction.
   */
  def apply(arn: String, clusterArn: String): EcsContainerInstanceArn =
    EcsContainerInstanceArn(Arn(arn), EcsClusterArn(clusterArn))
}

final case class EcsContainerInstance(
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
