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
package social.midas.discovery.schema

import sangria.schema.{ fields, Field, ListType }

import social.midas.discovery.common.{AbstractContext, AbstractSchema }
import social.midas.discovery.aws.ec2.{
  Ec2Client, Ec2Filter, Ec2Instance, Fetchers,
}

object Ec2Schema extends AbstractSchema {

  val rootFields = fields[AbstractContext,Unit](
    Field(
      "ec2Instances",
      ListType(Ec2Instance.Type),
      arguments = Ec2Filter.Args :: Nil,
      description = Some("The list of EC2 instances."),
      resolve = { ctx =>
        Ec2Client(ctx.ctx).describeInstances(
          filters=ctx.arg(Ec2Filter.Args).getOrElse(Seq.empty),
        ).unsafeToFuture(),
      },
    ),
  )

  override val fetchers = Seq(Fetchers.ec2DescribeInstances)
}
