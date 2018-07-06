/**
 * Copyright 2018 Midas Technologies AG
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
