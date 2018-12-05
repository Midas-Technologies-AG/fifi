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
package social.midas.discovery.aws

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import sangria.macros._
import scala.collection.immutable.{ ListMap, Vector }
import scala.concurrent.duration._
import scala.concurrent.Await

import social.midas.discovery.common
import social.midas.discovery.common.Ip4Extractor

class ExtractorsSpec(implicit ee: ExecutionEnv) extends Specification {

  "ip4Extractor" >> {
    "on constructed result" >> {
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
      val fut = common.prepareQuery(
        query, extractors=List(Ip4Extractor),
      )
      val result = Await.result(fut, 10.seconds)
      result.userContext.extractor must beSome
      val example =
        ListMap("data"
          -> ListMap("ecsClusters"
            -> Vector(ListMap("tasks"
              -> Vector(ListMap("containerInstance"
                -> ListMap("ec2Instance"
                  -> ListMap("privateIpAddress" -> "0.0.0.0"))))))))
      val extractor = result.userContext.extractor.get
      val extracted = extractor(example)
      extracted must_=== Seq("0.0.0.0")
    }
  }
}
