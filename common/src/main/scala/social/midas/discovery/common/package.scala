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
package social.midas.discovery

import org.apache.logging.log4j.scala.Logging
import sangria.ast.Document
import sangria.execution.{ Executor, ExecutionScheme }
import sangria.execution.deferred.HasId
import sangria.marshalling.ResultMarshaller
import sangria.parser.QueryParser
import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.JavaConverters._
import scala.util.Success

package object common extends Logging {

  val defaultContext = DefaultContext(None)

  /**
   * Restore the order of `result` as found in `original`.
   */
  def restoreOrder[Id, Res]
  (original: Seq[Id])
  (result: Seq[Res])
  (implicit id: HasId[Res, Id])
      : Seq[Res] = {
    val resultMap = Map[Id, Res](result.map(r => (id.id(r), r)): _*)
    original.map(resultMap(_))
  }

  def executeQuery(
    query: Document,
    ctx: AbstractContext = defaultContext,
    extractors: List[Extractor] = List.empty,
  )(implicit ec: ExecutionContext,
    marshaller: ResultMarshaller,
    scheme: ExecutionScheme,
  ): scheme.Result[AbstractContext,marshaller.Node] = {
    val (root, resolver) = SchemaFinder(ctx).findRootAndResolver()
    Executor.execute(
      root, query, ctx,
      deferredResolver=resolver,
      queryReducers=extractors,
    )
  }

  // TODO: Find Type
  def prepareQuery(
    query: Document,
    ctx: AbstractContext = defaultContext,
    extractors: List[Extractor] = List.empty,
  )(implicit ec: ExecutionContext
  ) = {
    logger.traceEntry(query, ctx, extractors)
    val (root, resolver) = SchemaFinder(ctx).findRootAndResolver()
    logger.traceExit(
      Executor.prepare(
        root, query, ctx,
        deferredResolver=resolver,
        queryReducers=extractors,
      )
    )
  }

  def discoverFromConfig[T]()(implicit ec: ExecutionContext)
      : Future[Seq[T]] = {
    logger.traceEntry()
    val queryConf = defaultContext.conf.getString("discovery.query")
    logger.debug(s"Got query from configuration: ${queryConf}")
    val Success(query: Document) = QueryParser.parse(queryConf)
    logger.debug(s"Parsed query: ${query}")
    val extractorsConf = defaultContext.conf
      .getStringList("discovery.extractors").asScala.toList
    logger.debug(s"Got extractors from configuration: ${extractorsConf}")
    val extractors = extractorsConf.map(getObject[Extractor](_))
    logger.debug(s"Resolved to extractors: ${extractors}")
    val prepared = prepareQuery(query, defaultContext, extractors)
    val result = prepared.flatMap(_.execute())
    logger.traceExit(
      if (extractorsConf == List.empty) {
        result.map(_.asInstanceOf[Seq[T]])
      } else {
        prepared.flatMap(p => {
          val extractor = p.userContext.extractor.get
          result.map(r => extractor(r).asInstanceOf[Seq[T]])
        })
      }
    )
  }

  /**
   * Loads the object from fully qualified class name `name`. 
   * 
   * NOTE: The fqcn has to end with a dollar-sign `$` to properly
   * denote an object. We do not try to correct wrong spellings here.
   */
  def getObject[T](name: String): T = {
    logger.traceEntry(name)
    val clazz = Class.forName(name).asInstanceOf[Class[_ <: T]]
    val obj = clazz.getDeclaredField("MODULE$")
    logger.traceExit(
      obj.get(null).asInstanceOf[T]
    )
  }
}
