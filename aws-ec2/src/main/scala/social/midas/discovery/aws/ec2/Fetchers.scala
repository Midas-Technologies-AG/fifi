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
