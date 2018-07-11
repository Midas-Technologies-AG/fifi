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

/**
 * Tests that specific schemata are go into the packages defining the
 * schema.
 */
class SchemaFinderSpec extends Specification {
  "should not find any schema without additional packages" >> {
    val finder = SchemaFinder()
    val result = finder.find()

    result should be empty
  }
}
