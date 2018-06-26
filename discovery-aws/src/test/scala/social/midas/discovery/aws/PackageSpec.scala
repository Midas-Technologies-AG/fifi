/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import scala.concurrent.duration._
import scala.concurrent.Await

import social.midas.test.Regex.Ip4Regex

class DiscoverIp4AddressesSpec(implicit ee: ExecutionEnv)
    extends Specification {
  "on real world" >> {
    val query = """
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
    val result = Await.result(
      discoverIp4Addresses("eu-central-1", query),
      10.seconds,
    )
    result must not be empty
    result must contain(matching(Ip4Regex)).foreach
  }
}
