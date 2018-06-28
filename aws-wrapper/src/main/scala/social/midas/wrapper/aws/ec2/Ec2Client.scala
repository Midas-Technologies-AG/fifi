/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ec2

import cats.effect.IO
import scala.collection.JavaConverters._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.{
  EC2AsyncClient, EC2AsyncClientBuilder,
}
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest

import social.midas.wrapper.aws
import social.midas.wrapper.aws.generic.AwsClient

/**
 * A wrapper for
 * [[software.amazon.awssdk.services.ec2.EC2AsyncClient]].
 */
case class Ec2Client(region: Region)
    extends AwsClient[EC2AsyncClientBuilder, EC2AsyncClient] {

  protected def builder = EC2AsyncClient.builder()

  /**
   * Fetches the description of EC2 instances grouped by their
   * reservations.
   * 
   * @param ids Only fetch EC2 instances having these instance ids.
   */
  def describeInstancesByReservation(ids: Seq[Ec2InstanceId] = Seq())
      : IO[Seq[Ec2Reservation]] = {
    val request = DescribeInstancesRequest.builder()
      .instanceIds(ids.map(_.unwrap).asJava)
      .build()
    onClient(_.describeInstances(request)).map(
      _.reservations.asScala.toSeq
        .map(Ec2Reservation(_))
    )
  }

  /**
   * Fetches the description of EC2 instances as in
   * [[describeInstancesByReservation]] but flattens the result to
   * only contain [[Ec2Instance]]s.
   */
  def describeInstances(ids: Seq[Ec2InstanceId] = Seq())
      : IO[Seq[Ec2Instance]] =
    describeInstancesByReservation(ids).map(_.map(_.instances).flatten)
}

object Ec2Client {

  /**
   * Build an [[Ec2Client]] using [[aws.region]].
   */
  def apply(): Ec2Client = Ec2Client(aws.region)
}
