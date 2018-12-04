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
  // networkInterfaces: Seq[EcsNetworkInterfaces],
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
