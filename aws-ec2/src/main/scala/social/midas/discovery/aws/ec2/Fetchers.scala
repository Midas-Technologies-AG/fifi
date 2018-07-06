/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws.ec2

import sangria.execution.deferred.{ Fetcher, HasId }

import social.midas.discovery.common.{ restoreOrder, AbstractContext }

object Fetchers {

  implicit val hasId = HasId[Ec2Instance, Ec2InstanceId](_.id)
  val ec2DescribeInstances = Fetcher[AbstractContext, Ec2Instance, Ec2InstanceId](
    (ctx: AbstractContext, instances: Seq[Ec2InstanceId]) => {
      Ec2Client(ctx).describeInstances(instances)
        .map(restoreOrder(instances))
        .unsafeToFuture()
    }
  )
}
