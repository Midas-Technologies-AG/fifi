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
package social.midas.discovery.aws.ec2

import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import scala.collection.JavaConverters._
import software.amazon.awssdk.services.ec2.model.Reservation


/**
 * A thin wrapper around an EC2 reservation id.
 * 
 * == JSON Deserialization ==
 * 
 * {{{
 * scala> import io.circe.literal._
 * scala> import social.midas.discovery.aws.ec2.Ec2ReservationId
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[Ec2ReservationId]
 * res0: io.circe.Decoder.Result[Ec2ReservationId] = Right(Ec2ReservationId(foo))
 * }}}
 */
final case class Ec2ReservationId(unwrap: String)

object Ec2ReservationId {

  /**
   * JSON encoder which simply unwraps the string (thin wrapper).
   */
  implicit val enc: Encoder[Ec2ReservationId] =
    new Encoder[Ec2ReservationId] {
      def apply(x: Ec2ReservationId): Json =
        Json.fromString(x.unwrap)
    }

  /**
   * JSON decoder which simply wraps up a string (thin wrapper).
   */
  implicit val dec: Decoder[Ec2ReservationId] =
    new Decoder[Ec2ReservationId] {
      def apply(c: HCursor): Decoder.Result[Ec2ReservationId] =
        c.as[String].map(Ec2ReservationId(_))
    }
}

/**
 * An EC2 reservation. Reservations basically group together one or
 * more EC2 instances.
 */
final case class Ec2Reservation(
  id: Ec2ReservationId,
  instances: Seq[Ec2Instance],
)

object Ec2Reservation {
  def apply(r: Reservation): Ec2Reservation =
    Ec2Reservation(
      Ec2ReservationId(r.reservationId()),
      r.instances().asScala.toSeq.map(Ec2Instance(_)),
    )

  /** JSON decoder. */
  implicit val dec: Decoder[Ec2Reservation] =
    deriveDecoder[Ec2Reservation]

  /** JSON encoder. */
  implicit val enc: Encoder[Ec2Reservation] =
    deriveEncoder[Ec2Reservation]
}
