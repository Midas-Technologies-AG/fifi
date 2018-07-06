/**
 * Copyright 2018 Midas Technologies AG
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
