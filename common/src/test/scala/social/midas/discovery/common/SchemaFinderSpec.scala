/**
 * Copyright 2018 Midas Technologies AG
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
