/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import sangria.macros._
import scala.collection.immutable.{ ListMap, Vector }
import scala.concurrent.duration._
import scala.concurrent.Await

import social.midas.discovery.common
import social.midas.discovery.common.{ Ip4Extractor, SchemaFinder }
import social.midas.test.Regex.Ip4Regex

class ExtractorsSpec(implicit ee: ExecutionEnv) extends Specification {

  "ip4Extractor" >> {
    "on constructed result" >> {
      val query = graphql"""
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
      val (root, resolver) =
        SchemaFinder(common.defaultContext).findRootAndResolver()
      val fut = common.prepareQuery(
        query, extractors=List(Ip4Extractor),
      )
      val result = Await.result(fut, 10.seconds)
      result.userContext.extractor must beSome
      val example =
        ListMap("data"
          -> ListMap("ecsClusters"
            -> Vector(ListMap("tasks"
              -> Vector(ListMap("containerInstance"
                -> ListMap("ec2Instance"
                  -> ListMap("privateIpAddress" -> "0.0.0.0"))))))))
      val extractor = result.userContext.extractor.get
      val extracted = extractor(example)
      extracted must_=== Seq("0.0.0.0")
    }
 
    "on real world" >> {
      val query = graphql"""
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
      val fut = common.prepareQuery(
        query, extractors=List(Ip4Extractor),
      )
      val prepared = Await.result(fut, 10.seconds)
      val extractor = prepared.userContext.extractor.get
      val result = Await.result(prepared.execute(), 10.seconds)
      val extracted = extractor(result).asInstanceOf[Seq[String]]
      extracted must not be empty
      extracted must contain(matching(Ip4Regex)).foreach
   }
  }
}
