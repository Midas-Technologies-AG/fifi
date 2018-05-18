/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ec2

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
 * scala> import social.midas.wrapper.aws.ec2.Ec2ReservationId
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[Ec2ReservationId]
 * res0: io.circe.Decoder.Result[Ec2ReservationId] = Right(Ec2ReservationId(foo))
 * }}}
 */
sealed case class Ec2ReservationId(unwrap: String)

object Ec2ReservationId {
  implicit val enc: Encoder[Ec2ReservationId] = new Encoder[Ec2ReservationId] {
    final def apply(x: Ec2ReservationId): Json = Json.fromString(x.unwrap)
  }
  implicit val dec: Decoder[Ec2ReservationId] = new Decoder[Ec2ReservationId] {
    final def apply(c: HCursor): Decoder.Result[Ec2ReservationId] =
      c.as[String].map(Ec2ReservationId(_))
  }
}

sealed case class Ec2Reservation(
  id: Ec2ReservationId,
  instances: Seq[Ec2Instance],
)

object Ec2Reservation {
  def apply(r: Reservation): Ec2Reservation =
    Ec2Reservation(
      Ec2ReservationId(r.reservationId()),
      r.instances().asScala.toSeq.map(Ec2Instance(_)),
    )

  implicit val dec: Decoder[Ec2Reservation] = deriveDecoder[Ec2Reservation]
  implicit val enc: Encoder[Ec2Reservation] = deriveEncoder[Ec2Reservation]
}
