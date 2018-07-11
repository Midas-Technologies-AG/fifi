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
import scala.concurrent.duration._
import scala.concurrent.Await

import social.midas.discovery.common
import social.midas.test.Regex.Ip4Regex

class PackageSpec(implicit ee: ExecutionEnv) extends Specification {
  "discover from config" >> {
    val result = Await.result(
      common.discoverFromConfig(), 10.seconds,
    ).asInstanceOf[Seq[String]]
    result must not be empty
    result must contain(matching(Ip4Regex)).foreach
  }
}
