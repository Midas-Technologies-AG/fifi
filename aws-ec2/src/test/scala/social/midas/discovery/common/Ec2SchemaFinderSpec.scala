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
package social.midas.discovery.common

import org.specs2.mutable.Specification

import social.midas.discovery.schema.Ec2Schema

class Ec2SchemaFinderSpec extends Specification {

  val finder = SchemaFinder()

  "should get our schema" >> {
    val result = getObject[AbstractSchema]("social.midas.discovery.schema.Ec2Schema$")
    result should_== Ec2Schema
  }

  "should find our Ec2Schema" >> {
    val result = finder.find()

    result must have size(1)
    result must contain(Ec2Schema)
  }
}
