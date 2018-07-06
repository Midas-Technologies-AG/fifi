/**
 * Copyright 2018 Midas Technologies AG
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
