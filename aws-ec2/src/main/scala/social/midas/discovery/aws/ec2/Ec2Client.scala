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

import cats.effect.IO
import scala.collection.JavaConverters._
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.{
  EC2AsyncClient, EC2AsyncClientBuilder,
}
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest

import social.midas.discovery.common.AbstractContext
import social.midas.discovery.common.aws
import social.midas.discovery.common.aws.AwsClient

/**
 * A wrapper for
 * [[software.amazon.awssdk.services.ec2.EC2AsyncClient]].
 */
final case class Ec2Client(region: Region)
    extends AwsClient[EC2AsyncClientBuilder, EC2AsyncClient] {

  protected def builder = EC2AsyncClient.builder()

  /**
   * Fetches the description of EC2 instances grouped by their
   * reservations.
   * 
   * @param ids only fetch EC2 instances having these instance ids
   * @param filters will be mapped to
   *   [[software.amazon.awssdk.services.ec2.model.Filter]]s and
   *   passed to the query
   */
  def describeInstancesByReservation(
    ids: Seq[Ec2InstanceId] = Seq.empty,
    filters: Seq[Ec2Filter] = Seq.empty,
  ): IO[Seq[Ec2Reservation]] = {
    val request = DescribeInstancesRequest.builder()
      .instanceIds(ids.map(_.unwrap).asJava)
      .filters(filters.map(_.toAws): _*)
      .build()
    withClient(_.describeInstances(request)).map(
      _.reservations.asScala.toSeq
        .map(Ec2Reservation(_))
    )
  }

  /**
   * Fetches the description of EC2 instances as in
   * [[describeInstancesByReservation]] but flattens the result to
   * only contain [[Ec2Instance]]s.
   */
  def describeInstances(
    ids: Seq[Ec2InstanceId] = Seq.empty,
    filters: Seq[Ec2Filter] = Seq.empty,
  )
      : IO[Seq[Ec2Instance]] =
    describeInstancesByReservation(ids, filters)
      .map(_.map(_.instances).flatten)
}

object Ec2Client {
  def apply(ctx: AbstractContext): Ec2Client =
    Ec2Client(aws.regionFromContext(ctx))

  def apply(): Ec2Client =
    Ec2Client(aws.region())
}
