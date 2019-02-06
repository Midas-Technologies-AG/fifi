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
package social.midas.external

import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.ecs.EcsAsyncClient
import software.amazon.awssdk.services.ecs.model.ListServicesRequest
import org.specs2.execute.{ AsResult, Error, Failure, Result, Skipped }
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach

import social.midas.discovery.common.aws.region

/**
 * Getting to know how AWS SDK works.
 */
class AwsEcsSpec extends Specification with ForEach[EcsAsyncClient] {

  def foreach[R: AsResult](f: EcsAsyncClient => R): Result = {
    val client = EcsAsyncClient.builder().region(region()).build()
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
    "`listContainerInstances()`" >> { client: EcsAsyncClient =>
      client.listContainerInstances().get must throwA[ExecutionException]
    }
    "`listServices()`" >> { client: EcsAsyncClient =>
      client.listServices().get must throwA[ExecutionException]
    }
  }

  "listing resources" >> { 
    "at least one cluster should be present" >> { client: EcsAsyncClient =>
      val response = client.listClusters().get
      response.clusterArns.isEmpty must_== false
    }

    "at least one service should be present" >> { client: EcsAsyncClient =>
      val clusterList = client.listClusters().get
      val clusterArn = clusterList.clusterArns.get(0)

      val req = ListServicesRequest.builder().cluster(clusterArn).build()
      val resp = client.listServices(req).get

      resp.serviceArns.isEmpty must_== false
    }
  }
}
