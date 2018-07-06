/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.schema

import sangria.schema.{ fields, Field, ListType }

import social.midas.discovery.common.{ AbstractContext, AbstractSchema }
import social.midas.discovery.common.aws.Arn
import social.midas.discovery.aws.ecs.{ EcsClient, EcsCluster, Fetchers }

object EcsSchema extends AbstractSchema {

  val rootFields = fields[AbstractContext, Unit](
    Field(
      "ecsClusters",
      ListType(EcsCluster.Type),
      arguments = Arn.Filter :: Nil,
      description = Some("The list of available ECS clusters."),
      resolve = { ctx =>
        EcsClient(ctx.ctx).listClusters(
          ctx.arg(Arn.Filter)
        ).unsafeToFuture()
      },
    ),
  )

  override val fetchers = Seq(
    Fetchers.ecsTaskDescriptions,
    Fetchers.ecsContainerInstances,
  )
}
