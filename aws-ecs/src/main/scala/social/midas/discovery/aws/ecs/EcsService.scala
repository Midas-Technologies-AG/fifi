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
