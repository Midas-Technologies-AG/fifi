/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws.ecs

import sangria.execution.deferred.{ Fetcher, HasId }
// TODO How can we eliminate this:
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import social.midas.discovery.common.{ restoreOrder, AbstractContext }

object Fetchers {


  implicit val EcsTaskId = HasId[EcsTask, EcsTaskArn](_.getFullArn) 

  val ecsTaskDescriptions: Fetcher[
    AbstractContext,
    EcsTask,
    EcsTask,
    EcsTaskArn
  ] = Fetcher[AbstractContext, EcsTask, EcsTaskArn](
    (ctx: AbstractContext, tasks: Seq[EcsTaskArn]) => {
      val byCluster = tasks.groupBy(_.clusterArn)
      Future.traverse(byCluster.toSeq)({
        case (cluster, tasksOfCluster) => {
          val arns = tasksOfCluster.map(_.arn)
          val client = EcsClient(ctx)
          client.describeTasks(cluster, arns).unsafeToFuture()
        }
      })
        .map(_.flatten.toSeq)
        .map(restoreOrder(tasks))
    }
  )

  implicit val EcsContainerInstanceId =
    HasId[EcsContainerInstance, EcsContainerInstanceArn](_.getArnPath)

  val ecsContainerInstances: Fetcher[
    AbstractContext,
    EcsContainerInstance,
    EcsContainerInstance,
    EcsContainerInstanceArn
  ] = Fetcher[
    AbstractContext,
    EcsContainerInstance,
    EcsContainerInstanceArn,
  ]( (ctx: AbstractContext, instances: Seq[EcsContainerInstanceArn]) => {
    val byCluster = instances.groupBy(_.clusterArn)
    Future.traverse(byCluster.toSeq)({
      case (cluster, instancesOfCluster) => {
        val arns = instancesOfCluster.map(_.arn)
        val client = EcsClient(ctx)
        client.describeContainerInstances(cluster, arns).unsafeToFuture()
      }
    })
      .map(_.flatten.toSeq)
      .map(restoreOrder(instances))
    }
  )
}
