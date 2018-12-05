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

import io.circe.{ Decoder, Json }
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import sangria.macros._
import sangria.marshalling.circe._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.matching.Regex

import social.midas.discovery.common
import social.midas.discovery.aws.ecs.{ EcsCluster, EcsTask }

class EcsSchemaSpec(implicit ee: ExecutionEnv) extends Specification {

  val nonExistentRegex = new Regex("non-existent")

  def extractClustersFromFutureJson[T](fut: Future[Json])(implicit dec: Decoder[T]) = {
    val doc: Json = Await.result(fut, 10.second)
    val cursor = doc.hcursor
    cursor.downField("data").downField("ecsClusters").as[T]
  }

  "general ecs listings" >> {
    "get all ecsClusters" >> {
      val query = graphql"""
query SomeEcsCluster {
  ecsClusters { arn }
}
      """
      val result = extractClustersFromFutureJson[Seq[EcsCluster]](
        common.executeQuery(query)
      )
      result must beRight
      result.right.get must not be empty
    }

    "filter ecsClusters" >> {
      val query = graphql"""
query SomeEcsCluster {
  ecsClusters(filterArn: "non-existent-cluster") { arn }
}
      """
      val result = extractClustersFromFutureJson[Seq[EcsCluster]](
        common.executeQuery(query)
      )
      result must beRight
      result.right.get must be empty
    }
  }

  "services" >> {
    "get from ecsClusters" >> {
      val query = graphql"""
query ContainerQuery {
  ecsClusters {
    arn
    services {
      arn
      clusterArn
    }
  }
}
      """
      val result = extractClustersFromFutureJson[Seq[EcsCluster]](
        common.executeQuery(query)
      )
      
      result must beRight
      result.right.get.head.services must beSome
      result.right.get.head.services.get must not be empty
    }
  }

  "tasks" >> {
    "get from ecsClusters" >> {
      val query = graphql"""
query ContainerQuery {
  ecsClusters {
    arn
    tasks {
      arn
      clusterArn
    }
  }
}
      """
      val result = extractClustersFromFutureJson[Seq[EcsCluster]](
        common.executeQuery( query)
      )
      
      result must beRight
      result.right.get.head.tasks must beSome
      result.right.get.head.tasks.get must not be empty
    }

    "completely resolving a task" >> {
      val query = graphql"""
query ContainerQuery {
  ecsClusters {
    arn
    tasks {
      arn
      clusterArn
      containers {
        arn
        clusterArn
        networkBindings {
          protocol
          bindIp
          containerPort
          hostPort
        }
      }
      group
    }
  }
}
      """
      val fut = common.executeQuery(query)
      val doc: Json = Await.result(fut, 10.second)
      val cursor = doc.hcursor
      val parsed = cursor.downField("data")
        .downField("ecsClusters")
        .downArray
        .downField("tasks")
        .as[Seq[EcsTask]]

      parsed must beRight
      parsed.right.get must not be empty
    }
  }
}
