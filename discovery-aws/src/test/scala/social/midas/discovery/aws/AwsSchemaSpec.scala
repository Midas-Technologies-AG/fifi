/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import io.circe.{ Decoder, Json }
import org.specs2.concurrent.ExecutionEnv
import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import sangria.execution._
import sangria.macros._
import sangria.marshalling.circe._
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.util.matching.Regex
import software.amazon.awssdk.core.regions.Region

import social.midas.wrapper.aws.ec2.Ec2Instance
import social.midas.wrapper.aws.ecs.{ EcsCluster, EcsTask }

trait WithAwsClients extends ForEach[AwsClients] {
  def foreach[R: AsResult](f: AwsClients => R): Result = {
    val client = new AwsClients(Region.EU_CENTRAL_1)
    try {
      AsResult(f(client))
    } finally {
      client.close
    }
  }
}

class AwsSchemaSpec(implicit ee: ExecutionEnv)
    extends Specification
    with WithAwsClients {

  val nonExistentRegex = new Regex("non-existent")

  def extractClustersFromFutureJson[T](fut: Future[Json])(implicit dec: Decoder[T]) = {
    val doc: Json = Await.result(fut, 10.second)
    val cursor = doc.hcursor
    cursor.downField("data").downField("ecsClusters").as[T]
  }

  "ecs" >> {
    "general ecs listings" >> {
      "get all ecsClusters" >> { clients: AwsClients =>
        val query = graphql"""
query SomeEcsCluster {
  ecsClusters { arn }
}
      """
        val result = extractClustersFromFutureJson[Seq[EcsCluster]](
          Executor.execute(AwsSchema.schema, query, clients)
        )
        result must beRight
        result.right must not be empty
      }

      "filter ecsClusters" >> { clients: AwsClients =>
        val query = graphql"""
query SomeEcsCluster {
  ecsClusters(filterArn: "non-existent-cluster") { arn }
}
      """
        val result = extractClustersFromFutureJson[Seq[EcsCluster]](
          Executor.execute(AwsSchema.schema, query, clients)
        )
        result must_== Right(Seq())
      }
    }

    "services" >> {
      "get from ecsClusters" >> { clients: AwsClients =>
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
          Executor.execute(AwsSchema.schema, query, clients)
        )
        
        result must beRight
        result.right.get.head.services must beSome
        result.right.get.head.services.get must not be empty
      }
    }

    "tasks" >> {
      "get from ecsClusters" >> { clients: AwsClients =>
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
          Executor.execute(AwsSchema.schema, query, clients)
        )
        
        result must beRight
        result.right.get.head.tasks must beSome
        result.right.get.head.tasks.get must not be empty
      }

      "completely resolving a task" >> { clients: AwsClients =>
        val query = graphql"""
query ContainerQuery {
  ecsClusters {
    arn
    tasks {
      arn
      clusterArn
      containerInstance {
        arn
        clusterArn
      }
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
        val fut =
          Executor.execute(AwsSchema.schema, query, clients, deferredResolver=Fetchers.resolver)

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

      "get private ip address of a task" >> { clients: AwsClients =>
        val query = graphql"""
query {
  ecsClusters {
    tasks {
      containerInstance {
        ec2Instance {
          privateIpAddress
        }
      }
    }
  }
}
"""
        val fut = Executor.execute(
          AwsSchema.schema, query, clients, deferredResolver=Fetchers.resolver,
        )
        val doc: Json = Await.result(fut, 10.second)
        val parsed = doc.hcursor.downField("data")
          .downField("ecsClusters")
          .downArray
          .downField("tasks")
          .downArray.downField("containerInstance")
          .downField("ec2Instance")
          .downField("privateIpAddress")
          .as[String]

        parsed must beRight
        parsed.right.get must startWith("172.")
      }
    }

    "container instances" >> {
      "get from ecsClusters" >> { clients: AwsClients =>
        val query = graphql"""
query ContainerQuery {
  ecsClusters {
    arn
    containerInstances {
      arn
      clusterArn
    }
  }
}
      """
        val result = extractClustersFromFutureJson[Seq[EcsCluster]](
          Executor.execute(AwsSchema.schema, query, clients)
        )
        
        result must beRight
        result.right.get.head.containerInstances must beSome
        result.right.get.head.containerInstances.get must not be empty
      }

      "resolve instances ip address" >> { clients: AwsClients =>
        val query = graphql"""
query ContainerQuery {
  ecsClusters {
    arn
    containerInstances {
      arn
      ec2Instance {
        privateIpAddress
      }
    }
  }
}
"""
        val fut =
          Executor.execute(AwsSchema.schema, query, clients, deferredResolver=Fetchers.resolver)

        val doc: Json = Await.result(fut, 10.second)
        val cursor = doc.hcursor
        val parsed = cursor.downField("data")
          .downField("ecsClusters")
          .downArray.downField("containerInstances")
          .downArray.downField("ec2Instance")
          .downField("privateIpAddress")
          .as[String]

        parsed must beRight
        parsed.right.get must startWith("172.")
      }
    }
  }

  "ec2" >> {
    "list instances" >> { clients: AwsClients =>
      val query = graphql"""
query Ec2Instances {
  ec2Instances {
    id
    privateIpAddress
  }
}
"""
      val fut = Executor.execute(AwsSchema.schema, query, clients)
      val doc: Json = Await.result(fut, 10.seconds)
      val parsed = doc.hcursor.downField("data")
        .downField("ec2Instances")
        .as[Seq[Ec2Instance]]

      parsed must beRight
      parsed.right.get must not be empty
    }
  }
}
