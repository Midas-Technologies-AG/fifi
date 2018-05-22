/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.external

import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.regions.Region.EU_CENTRAL_1
import software.amazon.awssdk.services.ecs.ECSAsyncClient
import software.amazon.awssdk.services.ecs.model.{
  ListContainerInstancesRequest, ListServicesRequest,
}
import org.specs2.execute.{ AsResult, Error, Failure, Result, Skipped }
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach

/**
 * Getting to know how AWS SDK works.
 */
class AwsEcsSpec extends Specification with ForEach[ECSAsyncClient] {

  def foreach[R: AsResult](f: ECSAsyncClient => R): Result = {
    val client = ECSAsyncClient.builder().region(EU_CENTRAL_1).build()
    lazy val networkDown = Skipped("Check your network connection!")
    try {
      AsResult(f(client)) match {
        case e1: Error => e1.exception match {
          case e2: ExecutionException => e2.getCause match {
            case e3: SdkClientException => e3.getCause match {
              case _: UnknownHostException => networkDown
              case _ => e1
            }
            case _ => e1
          }
          case _ => e1
        }
        case failure: Failure => if (
          failure.message.contains("java.net.UnknownHostException")
        ) {
          networkDown
        } else {
          failure
        }
        case other => other
      }
    } finally {
      client.close()
    }
  }

  "ClusterNotFoundException on" >> {
    "`listContainerInstances()`" >> { client: ECSAsyncClient =>
      client.listContainerInstances().get must throwA[ExecutionException]
        .like({ case exc =>
          exc.getCause.getMessage() must startWith("Cluster not found.")
        })
    }
    "`listServices()`" >> { client: ECSAsyncClient =>
      client.listServices().get must throwA[ExecutionException]
        .like({ case exc =>
          exc.getCause.getMessage() must startWith("Cluster not found.")
        })
    }
  }

  "listing resources" >> { 
    "at least one cluster should be present" >> { client: ECSAsyncClient =>
      val response = client.listClusters().get
      response.clusterArns.isEmpty must_== false
    }

    "at least one container instance should be present" >> { client: ECSAsyncClient =>
      val clusterList = client.listClusters().get
      val clusterArn = clusterList.clusterArns.get(0)

      val req = ListContainerInstancesRequest.builder()
        .cluster(clusterArn)
        .build()
      val resp = client.listContainerInstances(req).get

      resp.containerInstanceArns.isEmpty must_== false
    }

    "at least one service should be present" >> { client: ECSAsyncClient =>
      val clusterList = client.listClusters().get
      val clusterArn = clusterList.clusterArns.get(0)

      val req = ListServicesRequest.builder().cluster(clusterArn).build()
      val resp = client.listServices(req).get

      resp.serviceArns.isEmpty must_== false
    }
  }
}
