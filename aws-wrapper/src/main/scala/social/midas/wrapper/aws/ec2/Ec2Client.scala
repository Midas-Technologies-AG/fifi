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

import social.midas.wrapper.aws.generic.AwsClient

case class Ec2Client(region: Region)
    extends AwsClient[EC2AsyncClientBuilder, EC2AsyncClient] {

  protected def builder = EC2AsyncClient.builder()

  /**
   * This is the `describeInstances` method in AWS SDK.
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

  def describeInstances(ids: Seq[Ec2InstanceId] = Seq())
      : IO[Seq[Ec2Instance]] =
    describeInstancesByReservation(ids).map(_.map(_.instances).flatten)
}
