/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import sangria.execution.deferred.{ DeferredResolver, Fetcher, HasId }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import social.midas.wrapper.aws.ec2.{
  Ec2Instance, Ec2InstanceId,
}
import social.midas.wrapper.aws.ecs.{
  EcsContainerInstance, EcsContainerInstanceArn, EcsTaskArn, EcsTask
}

object Fetchers {

  def restoreOrder[Id, Res]
  (original: Seq[Id])
  (result: Seq[Res])
  (implicit id: HasId[Res, Id])
      : Seq[Res] = {
    val resultMap = Map[Id, Res](result.map(r => (id.id(r), r)): _*)
    original.map(resultMap(_))
  }

  implicit val Ec2InstanceId = HasId[Ec2Instance, Ec2InstanceId](_.id)

  val ec2DescribeInstances = Fetcher[AwsClients, Ec2Instance, Ec2InstanceId](
    (clients: AwsClients, instances: Seq[Ec2InstanceId]) => {
      clients.ec2.describeInstances(instances)
        .map(restoreOrder(instances))
        .unsafeToFuture()
    }
  )

  implicit val EcsTaskId = HasId[EcsTask, EcsTaskArn](_.getFullArn) 

  val ecsTaskDescriptions = Fetcher[AwsClients, EcsTask, EcsTaskArn](
    (clients: AwsClients, tasks: Seq[EcsTaskArn]) => {
      val byCluster = tasks.groupBy(_.clusterArn)
      Future.traverse(byCluster.toSeq)({
        case (cluster, tasksOfCluster) => {
          val arns = tasksOfCluster.map(_.arn)
          clients.ecs.describeTasks(cluster, arns).unsafeToFuture()
        }
      })
        .map(_.flatten.toSeq)
        .map(restoreOrder(tasks))
    }
  )

  implicit val EcsContainerInstanceId =
    HasId[EcsContainerInstance, EcsContainerInstanceArn](_.getArnPath)

  val ecsContainerInstances = Fetcher[
    AwsClients,
    EcsContainerInstance,
    EcsContainerInstanceArn,
  ]( (clients: AwsClients, instances: Seq[EcsContainerInstanceArn]) => {
    val byCluster = instances.groupBy(_.clusterArn)
    Future.traverse(byCluster.toSeq)({
      case (cluster, instancesOfCluster) => {
        val arns = instancesOfCluster.map(_.arn)
        clients.ecs.describeContainerInstances(cluster, arns).unsafeToFuture()
      }
    })
      .map(_.flatten.toSeq)
      .map(restoreOrder(instances))
    }
  )

  val resolver: DeferredResolver[AwsClients] =
    DeferredResolver.fetchers(ecsContainerInstances, ecsTaskDescriptions, ec2DescribeInstances)
}
