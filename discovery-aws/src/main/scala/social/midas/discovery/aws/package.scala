/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery

import sangria.ast.Document
import sangria.execution.Executor
import sangria.parser.QueryParser
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

package object aws {

  def discoverIp4Addresses(region: String, query: String)
    (implicit ec: ExecutionContext)
      : Future[Seq[String]] = {
    val Success(parsedQuery: Document) = QueryParser.parse(query)
    val clients = AwsClients(region)
    val ctx = AwsContext(clients)
    for {
      prepared <- Executor.prepare(
        AwsSchema.schema,
        parsedQuery,
        ctx,
        deferredResolver=Fetchers.resolver,
        queryReducers=List(Extractors.ip4Extractor),
      )

      extractor = prepared.userContext.extractor.get
      result <- prepared.execute()
    } yield (extractor(result))
  }

}
