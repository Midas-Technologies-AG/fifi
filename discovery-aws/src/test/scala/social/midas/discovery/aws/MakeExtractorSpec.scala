/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import sangria.execution.Executor
import sangria.macros._
import scala.collection.immutable.{ ListMap, Vector }
import scala.concurrent.duration._
import scala.concurrent.Await

import social.midas.test.Regex.Ip4Regex

class ExtractorsSpec(implicit ee: ExecutionEnv)
    extends Specification
    with WithAwsContext {
  "ip4Extractor" >> {
    "on constructed result" >> { ctx: AwsContext =>
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
      val fut = Executor.prepare(
        AwsSchema.schema, query, ctx,
        deferredResolver=Fetchers.resolver,
        queryReducers=List(Extractors.ip4Extractor),
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

    "on real world" >> { ctx: AwsContext =>
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
      val preparedFut = Executor.prepare(
        AwsSchema.schema, query, ctx,
        deferredResolver=Fetchers.resolver,
        queryReducers=List(Extractors.ip4Extractor),
      )
      val prepared = Await.result(preparedFut, 10.seconds)
      val extractor = prepared.userContext.extractor.get
      val result = Await.result(prepared.execute(), 10.seconds)
      val extracted = extractor(result)
      extracted must not be empty
      extracted must contain(matching(Ip4Regex)).foreach
    }
  }
}
