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

import io.circe.Json
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import sangria.macros._
import sangria.marshalling.circe._
import scala.concurrent.Await
import scala.concurrent.duration._

import social.midas.discovery.aws.ec2.Ec2Instance
import social.midas.discovery.common

class Ec2SchemaSpec(implicit ee: ExecutionEnv) extends Specification {

  "list instances" >> { 
    val query = graphql"""
query Ec2Instances {
  ec2Instances {
    id
    privateIpAddress
  }
}
"""
    val doc: Json = Await.result(common.executeQuery(query), 10.seconds)
    val parsed = doc.hcursor.downField("data")
      .downField("ec2Instances")
      .as[Seq[Ec2Instance]]

    parsed must beRight
    parsed.right.get must not be empty
  }

  "using filters" >> {
    val query = graphql"""
query {
  ec2Instances(
    filters: [{name: "tag:Name", values: ["non-existent"]}]
  ) {
    id
  }
} 
"""
    val doc: Json = Await.result(common.executeQuery(query), 10.seconds)
    val extracted = doc.hcursor.downField("data")
      .downField("ec2Instances")
      .as[Seq[Ec2Instance]]

    extracted must beRight
    extracted.right.get must be empty
  }
}
