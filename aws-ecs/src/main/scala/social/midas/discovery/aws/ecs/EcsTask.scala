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
package social.midas.discovery.aws.ecs

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import org.apache.logging.log4j.scala.Logging
import sangria.schema.{
  fields, Argument, DeferredValue, Field, ListType, ObjectType,
  OptionType, OptionInputType, StringType,
}
import scala.collection.JavaConverters._
// TODO How can we eliminate this:
import scala.concurrent.ExecutionContext.Implicits.global
import software.amazon.awssdk.services.ecs.model.Task

import social.midas.discovery.common.AbstractContext
import social.midas.discovery.common.aws.{ Arn, ArnLike }

/**
 * The ARN of an ECS container with it's cluster ARN.
 */
final case class EcsTaskArn(
  arn: Arn,
  clusterArn: EcsClusterArn,
) extends ArnLike

object EcsTaskArn {
  implicit val dec: Decoder[EcsTaskArn] = deriveDecoder[EcsTaskArn]
  implicit val enc: Encoder[EcsTaskArn] = deriveEncoder[EcsTaskArn]
}

/**
 * The description of an ECS task.
 */
final case class EcsTask(
  arn: Arn,
  clusterArn: EcsClusterArn,
  containerInstance: Option[EcsContainerInstanceArn],
  containers: Seq[EcsContainer],
  group: String,
) extends ArnLike {
  def getFullArn = EcsTaskArn(arn, clusterArn)
}

object EcsTask extends Logging {
  implicit val dec: Decoder[EcsTask] = deriveDecoder[EcsTask]
  implicit val enc: Encoder[EcsTask] = deriveEncoder[EcsTask]


  def apply(task: Task): EcsTask = {
    logger.traceEntry(task)
    val res = EcsTask(
      Arn(task.taskArn()),
      EcsClusterArn(task.clusterArn()),
      Option(task.containerInstanceArn()).map(
        EcsContainerInstanceArn(_, task.clusterArn())
      ),
      containers = task.containers().asScala.toSeq.map(x => EcsContainer(task.clusterArn(), x)),
      group      = task.group(),
    )
    logger.traceExit(res)
  }

  implicit val Type = ObjectType(
    "EcsTask",
    "An ECS task",
    fields[AbstractContext, EcsTaskArn](
      Field("arn", Arn.Type, resolve = _.value.arn),
      Field("clusterArn", EcsClusterArn.Type, resolve = _.value.clusterArn),
      Field("containerInstance", OptionType(EcsContainerInstanceArn.Type),
        description = Some("The container instance that hosts the task."),
        resolve = ctx => DeferredValue(
          Fetchers.ecsTaskDescriptions.defer(ctx.value)
        ).map(_.containerInstance),
      ),
      Field("containers",
        ListType(EcsContainer.Type),
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

  val FamilyFilter = Argument(
    "family", OptionInputType(StringType),
    description="The name of the family with which to filter the tasks results.",
  )  
}
