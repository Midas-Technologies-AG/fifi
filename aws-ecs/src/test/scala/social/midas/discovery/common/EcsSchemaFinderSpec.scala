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

import social.midas.discovery.common
import social.midas.discovery.schema.{ Ec2Schema, EcsSchema }

class EcsSchemaFinderSpec extends Specification {

  val finder = SchemaFinder()
  val schemata = finder.find()

  "should get EcsSchema" >> {
    val result = common.getObject[AbstractSchema]("social.midas.discovery.schema.EcsSchema$")
    result should_== EcsSchema
  }

  "should find Ec2Schema and EcsSchema" >> {
    schemata must have size(2)
    schemata must contain(Ec2Schema)
    schemata must contain(EcsSchema)
  }
}
