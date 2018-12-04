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

import org.specs2.mutable.Specification
import scala.util.matching.Regex

class EcsClientSpec extends Specification {

  val client = EcsClient()

  "`clusters` yields at least one result" >> {
    val result: Seq[EcsClusterArn] = client.listClusters().unsafeRunSync()
    result must not be empty
  }

  "`filter` argument works" >> {
    val nonExistentRe = new Regex("non-existent")
    val result: Seq[EcsClusterArn] =
      client.listClusters(Some(nonExistentRe)).unsafeRunSync()
    result must be empty
  }
}
