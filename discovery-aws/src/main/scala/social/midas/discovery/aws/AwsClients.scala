/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import software.amazon.awssdk.core.regions.Region

import social.midas.wrapper.aws.ec2.Ec2Client
import social.midas.wrapper.aws.ecs.EcsClient

case class AwsClients(region: Region) {
  lazy val ec2: Ec2Client = Ec2Client(region)
  lazy val ecs: EcsClient = EcsClient(region)

  def close(): Unit = {
    ec2.close()
    ecs.close()
  }
}

object AwsClients {
  def apply(region: String): AwsClients = AwsClients(Region.of(region))
}
