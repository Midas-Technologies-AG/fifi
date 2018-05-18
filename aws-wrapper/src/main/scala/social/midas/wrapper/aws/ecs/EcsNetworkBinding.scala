/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import software.amazon.awssdk.services.ecs.model.NetworkBinding

case class EcsNetworkBinding(
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
}
