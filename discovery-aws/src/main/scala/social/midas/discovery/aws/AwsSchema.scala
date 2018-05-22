/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex
import sangria.schema._
import sangria.macros.derive._

import social.midas.wrapper.aws.generic.Arn
import social.midas.wrapper.aws.ecs._
import social.midas.wrapper.aws.ec2._

object AwsSchema {

  implicit val ArnType = ScalarAlias[Arn, String](
    StringType, _.unwrap, x => Right(Arn(x)),
  )

  implicit val RegexType = ScalarAlias[Regex, String](
    StringType, _.regex, x => Right(new Regex(x)),
  )

  val FilterArn = Argument(
    "filterArn",
    OptionInputType(RegexType),
    description = "Filter ARNs by matching to a regular expression.",
  )

  implicit val EcsClusterArnType = ScalarAlias[EcsClusterArn, String](
    StringType, _.arn.unwrap, x => Right(EcsClusterArn(Arn(x))),
  )

  implicit val Ec2InstanceIdType = ObjectType(
    "Ec2Instance",
    "Describes an EC2 instance.",
    fields[AwsClients, Ec2InstanceId](
      Field("id", StringType, resolve = _.value.unwrap),
      Field("privateIpAddress", StringType,
        resolve = ctx => DeferredValue(
          Fetchers.ec2DescribeInstances.defer(ctx.value),
        ).map(_.privateIpAddress),
      ),
    ),
  )

  implicit val Ec2InstanceType = deriveObjectType[Unit, Ec2Instance](
    ObjectTypeDescription("Describes an EC2 instance."),
    ReplaceField("id", Field("id", StringType, resolve = _.value.id.unwrap)),
  )
      
  implicit val EcsContainerInstanceType = ObjectType(
    "EcsContainerInstance",
    "An EC2 instance that is running the Amazon ECS agent and has been registered with a cluster.",
    fields[AwsClients, EcsContainerInstanceArn](
      Field("arn", ArnType, resolve = _.value.arn),
      Field("clusterArn", EcsClusterArnType, resolve = _.value.clusterArn),
      Field("ec2Instance", Ec2InstanceIdType,
        resolve = ctx => DeferredValue(
          Fetchers.ecsContainerInstances.defer(ctx.value)
        ).map(_.ec2Instance),
      ),
    ),
  )

  implicit val EcsNetworkBindingType = deriveObjectType[Unit, EcsNetworkBinding](
    ObjectTypeDescription(
      "Details on the network bindings between a container and its host container instance."
    ),
  )

  implicit  val EcsContainerType = deriveObjectType[Unit, EcsContainer](
    ObjectTypeDescription("An ECS container"),
  )

  implicit val EcsServiceType = deriveObjectType[Unit, EcsServiceArn](
    ObjectTypeDescription("An ECS service"),
  )

  implicit val EcsTaskType = ObjectType(
    "EcsTask",
    "An ECS task",
    fields[AwsClients, EcsTaskArn](
      Field("arn", ArnType, resolve = _.value.arn),
      Field("clusterArn", EcsClusterArnType, resolve = _.value.clusterArn),
      Field("containerInstance", EcsContainerInstanceType,
        description = Some("The container instance that hosts the task."),
        resolve = ctx => DeferredValue(
          Fetchers.ecsTaskDescriptions.defer(ctx.value)
        ).map(_.containerInstance),
      ),
      Field("containers",
        OptionType(ListType(EcsContainerType)),
        description = Some("The containers associated with the task."),
        resolve = ctx => DeferredValue(
          Fetchers.ecsTaskDescriptions.defer(ctx.value)
        ).map(_.containers),
      ),
      Field("group", StringType,
        description = Some("The name of the task group associated with the task."),
        resolve = ctx => DeferredValue(
          Fetchers.ecsTaskDescriptions.defer(ctx.value)
        ).map(_.group),
      ),
    ),
  )

  val EcsTaskFamilyFilter = Argument(
    "family", OptionInputType(StringType),
    description="The name of the family with which to filter the tasks results.",
  )
  
  implicit val EcsClusterType = ObjectType(
    "EcsCluster",
    "An AWS ECS cluster.",
    fields[AwsClients, EcsClusterArn](
      Field("arn", ArnType, resolve = _.value.arn),
      Field(
        "containerInstances",
        OptionType(ListType(EcsContainerInstanceType)),
        arguments = FilterArn :: Nil,
        description = Some("The list of containers of this cluster matching filterArn if specified."),
        resolve = { ctx =>
          ctx.ctx.ecs.listContainerInstances(ctx.value, ctx.arg(FilterArn)).unsafeToFuture()
        },
      ),
      Field(
        "services",
        OptionType(ListType(EcsServiceType)),
        arguments = FilterArn :: Nil,
        description = Some("The list of services of this cluster matching filterArn if specified."),
        resolve = { ctx =>
          ctx.ctx.ecs.listServices(ctx.value, ctx.arg(FilterArn)).unsafeToFuture()
        },
      ),
      Field(
        "tasks",
        OptionType(ListType(EcsTaskType)),
        arguments = FilterArn :: EcsTaskFamilyFilter :: Nil,
        description = Some("The list of tasks of this cluster matching filterArn if specified."),
        resolve = { ctx =>
          ctx.ctx.ecs.listTasks(
            ctx.value,
            ctx.arg(FilterArn),
            ctx.arg(EcsTaskFamilyFilter),
          ).unsafeToFuture()
        },
      ),
    )
  )

  val RootDiscoveryType = ObjectType(
    "RootDiscovery",
    fields[AwsClients, Unit](
      Field(
        "ecsClusters",
        ListType(EcsClusterType),
        arguments = FilterArn :: Nil,
        description = Some("The list of available ECS clusters."),
        resolve = { ctx =>
          ctx.ctx.ecs.listClusters(ctx.arg(FilterArn)).unsafeToFuture()
        },
      ),
      Field(
        "ec2Instances",
        ListType(Ec2InstanceType),
        description = Some("The list of EC2 instances."),
        resolve = _.ctx.ec2.describeInstances().unsafeToFuture(),
      ),
    )
  )

  val schema = Schema(RootDiscoveryType)
}
