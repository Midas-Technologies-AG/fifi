/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws.ecs

import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import sangria.schema.{
  fields, Field, ListType, ObjectType, ScalarAlias, StringType,
}

import social.midas.discovery.common.AbstractContext
import social.midas.discovery.common.aws.{ Arn, ArnLike }

/**
 * A thin wrapper around an ARN of an ECS cluster. 
 * 
 * == JSON Deserialization ==
 * 
 * {{{
 * scala> import io.circe.literal._
 * scala> import social.midas.discovery.aws.ecs.EcsClusterArn
 * 
 * scala> (json"""
 *      | "foo"
 *      | """).as[EcsClusterArn]
 * res0: io.circe.Decoder.Result[EcsClusterArn] = Right(EcsClusterArn(Arn(foo)))
 * }}}
 */
final case class EcsClusterArn(arn: Arn) extends ArnLike

object EcsClusterArn {
  def apply(arn: String): EcsClusterArn = EcsClusterArn(Arn(arn))

  /** JSON decoder. */
  implicit val dec: Decoder[EcsClusterArn] = new Decoder[EcsClusterArn] {
    def apply(c: HCursor): Decoder.Result[EcsClusterArn] =
      c.as[String].map(x => EcsClusterArn(Arn(x)))
  }

  /** JSON encoder. */
  implicit val enc: Encoder[EcsClusterArn] = new Encoder[EcsClusterArn] {
    def apply(x: EcsClusterArn): Json = Json.fromString(x.arn.unwrap)
  }

  implicit val Type = ScalarAlias[EcsClusterArn, String](
    StringType, _.arn.unwrap, x => Right(EcsClusterArn(Arn(x))),
  )  
}

/**
 * Product object for ECS clusters and related information.
 */
final case class EcsCluster(
  arn: EcsClusterArn,
  containerInstances: Option[Seq[EcsContainerInstanceArn]] = None,
  services: Option[Seq[EcsServiceArn]] = None,
  tasks: Option[Seq[EcsTaskArn]] = None,
)

object EcsCluster {
  /** JSON encoder. */
  implicit val dec: Decoder[EcsCluster] = deriveDecoder[EcsCluster]

  /** JSON decoder. */
  implicit val enc: Encoder[EcsCluster] = deriveEncoder[EcsCluster]

  implicit val Type = ObjectType(
    "EcsCluster",
    "An AWS ECS cluster.",
    fields[AbstractContext, EcsClusterArn](
      Field("arn", Arn.Type, resolve = _.value.arn),
      Field(
        "containerInstances",
        ListType(EcsContainerInstanceArn.Type),
        arguments = Arn.Filter :: Nil,
        description = Some("The list of containers of this cluster matching filterArn if specified."),
        resolve = { ctx =>
          EcsClient(ctx.ctx).listContainerInstances(ctx.value, ctx.arg(Arn.Filter)).unsafeToFuture()
        },
      ),
      Field(
        "services",
        ListType(EcsServiceArn.Type),
        arguments = Arn.Filter :: Nil,
        description = Some("The list of services of this cluster matching filterArn if specified."),
        resolve = { ctx =>
          EcsClient(ctx.ctx).listServices(ctx.value, ctx.arg(Arn.Filter)).unsafeToFuture()
        },
      ),
      Field(
        "tasks",
        ListType(EcsTask.Type),
        arguments = Arn.Filter :: EcsTask.FamilyFilter :: Nil,
        description = Some("The list of tasks of this cluster."),
        resolve = { ctx =>
          EcsClient(ctx.ctx).listTasks(
            ctx.value,
            ctx.arg(Arn.Filter),
            ctx.arg(EcsTask.FamilyFilter),
          ).unsafeToFuture()
        },
      ),
    )
  )
}
