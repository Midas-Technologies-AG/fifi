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
import software.amazon.awssdk.services.ecs.model.NetworkBinding

final case class EcsNetworkBinding(
  protocol: String, // TODO: Change to enume
  bindIp: String,
  containerPort: Int,
  hostPort: Int,
)

object EcsNetworkBinding {
  implicit val dec: Decoder[EcsNetworkBinding] = deriveDecoder[EcsNetworkBinding]
  implicit val enc: Encoder[EcsNetworkBinding] = deriveEncoder[EcsNetworkBinding]

  def apply(binding: NetworkBinding): EcsNetworkBinding =
    EcsNetworkBinding(
      binding.protocol().toString(),
      binding.bindIP(),
      binding.containerPort(),
      binding.hostPort(),
    )

  implicit val EcsNetworkBindingType = deriveObjectType[Unit, EcsNetworkBinding](
    ObjectTypeDescription(
      "Details on the network bindings between a container and its host container instance."
    ),
  )
}
