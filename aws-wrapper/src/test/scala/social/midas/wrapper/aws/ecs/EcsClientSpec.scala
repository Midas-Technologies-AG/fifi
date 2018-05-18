/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ecs

import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import org.specs2.mutable.Specification
import scala.util.matching.Regex
import software.amazon.awssdk.core.regions.Region

trait WithEcsClient extends ForEach[EcsClient] {
  def foreach[R: AsResult](f: EcsClient => R): Result = {
    val client = EcsClient(Region.EU_CENTRAL_1)
    try {
      AsResult(f(client))
    } finally {
      client.close()
    }
  }
} 

class EcsClientSpec extends Specification with WithEcsClient {
  
  "`clusters` yields at least one result" >> { client: EcsClient =>
    val result: Seq[EcsClusterArn] = client.listClusters().unsafeRunSync()
    result must not be empty
  }

  "`filter` argument works" >> { client: EcsClient =>
    val nonExistentRe = new Regex("non-existent")
    val result: Seq[EcsClusterArn] =
      client.listClusters(Some(nonExistentRe)).unsafeRunSync()
    result must be empty
  }

  "listing `containers` and filter on EcsCluster" >> { client: EcsClient =>
    val prog = for {
      clusters <- client.listClusters()
      clusterWithContainers <- client.listContainerInstances(clusters.head)
    } yield clusterWithContainers

    val result = prog.unsafeRunSync()

    result must not be empty
  }
}
