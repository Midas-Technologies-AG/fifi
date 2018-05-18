/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import cats.effect.IO
import java.util.concurrent.CompletableFuture
import scala.collection.JavaConverters._
import scala.util.matching.Regex
import software.amazon.awssdk.core.regions.Region
import software.amazon.awssdk.services.ecs.{
  ECSAsyncClient, ECSAsyncClientBuilder
}
import software.amazon.awssdk.services.ecs.model.{
  DescribeContainerInstancesRequest,
  DescribeTasksRequest,
  ListClustersResponse,
  ListContainerInstancesRequest, ListContainerInstancesResponse,
  ListServicesRequest, ListServicesResponse,
  ListTasksRequest, ListTasksResponse,
}

import social.midas.wrapper.aws.generic.{ Arn, ArnLike }
import social.midas.wrapper.aws.generic.AwsClient

case class EcsClient(region: Region)
    extends AwsClient[ECSAsyncClientBuilder, ECSAsyncClient] {

  def builder = ECSAsyncClient.builder()

  def listClusters(filterArn: Option[Regex] = None)
      : IO[Seq[EcsClusterArn]] =
    queryListExtractTransformMatch[ListClustersResponse, EcsClusterArn](
      _.listClusters(),
      _.clusterArns,
      EcsClusterArn(_),
      filterArn
    )

  def listContainerInstances(cluster: EcsClusterArn, filterArn: Option[Regex] = None)
      : IO[Seq[EcsContainerInstanceArn]] = {
    val request = ListContainerInstancesRequest.builder()
      .cluster(cluster.arn.unwrap)
      .build()
    queryListExtractTransformMatch[ListContainerInstancesResponse, EcsContainerInstanceArn](
      _.listContainerInstances(request),
      _.containerInstanceArns,
      x => EcsContainerInstanceArn(Arn(x), cluster),
      filterArn,
    )
  }

  def listServices(cluster: EcsClusterArn, filterArn: Option[Regex] = None)
      : IO[Seq[EcsServiceArn]] = {
    val request = ListServicesRequest.builder().cluster(cluster.arn.unwrap).build()
    queryListExtractTransformMatch[ListServicesResponse, EcsServiceArn](
      _.listServices(request),
      _.serviceArns,
      x => EcsServiceArn(Arn(x), cluster),
      filterArn,
    )
  }

  def listTasks(
    cluster: EcsClusterArn,
    filterArn: Option[Regex] = None,
    family: Option[String] = None,
  ) : IO[Seq[EcsTaskArn]] = {
    val builder = ListTasksRequest.builder()
      .cluster(cluster.arn.unwrap)
    val request = family.map(builder.family(_)).getOrElse(builder)
        .build()

    queryListExtractTransformMatch[ListTasksResponse, EcsTaskArn](
      _.listTasks(request),
      _.taskArns,
      x => EcsTaskArn(Arn(x), cluster),
      filterArn,
    )
  }

  def describeTasks(cluster: EcsClusterArn, tasks: Seq[Arn])
      : IO[Seq[EcsTask]] = {
    val request = DescribeTasksRequest.builder()
      .cluster(cluster.arn.unwrap)
      .tasks(tasks.map(_.arn.unwrap).asJava)
      .build()
    onClient(_.describeTasks(request)).map(
      _.tasks.asScala.toSeq.map(EcsTask.apply)
    )
  }

  def describeContainerInstances(cluster: EcsClusterArn, instances: Seq[Arn]):
      IO[Seq[EcsContainerInstance]] = {
    val request = DescribeContainerInstancesRequest.builder()
      .cluster(cluster.arn.unwrap)
      .containerInstances(instances.map(_.arn.unwrap).asJava)
      .build()
    onClient(_.describeContainerInstances(request)).map(
      _.containerInstances.asScala.toSeq.map(EcsContainerInstance(cluster, _))
    )
  }

  def queryListExtractTransformMatch[R,T <: ArnLike](
    list: ECSAsyncClient => CompletableFuture[R],
    extract: R => java.util.List[String],
    transform: String => T,
    maybeRegex: Option[Regex] = None,
  ): IO[Seq[T]] = {
    onClient(list).map({ response =>
      val extracted = extract(response)
      val transformed = extracted.asScala.toSeq.map(transform)
      maybeRegex match {
        case None => transformed
        case Some(r) => transformed.filter(_.arnMatches(r))
      }
    })
  }
}

