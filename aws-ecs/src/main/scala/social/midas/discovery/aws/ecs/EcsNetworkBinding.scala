/**
 * Copyright 2018 Midas Technologies AG
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
