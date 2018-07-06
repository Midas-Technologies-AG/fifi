/**
 * Copyright 2018 Midas Technologies AG
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
