/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery

import sangria.ast.Document
import sangria.execution.{ Executor, ExecutionScheme }
import sangria.execution.deferred.HasId
import sangria.marshalling.ResultMarshaller
import sangria.parser.QueryParser
import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.JavaConverters._
import scala.util.Success

package object common {

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
    val (root, resolver) = SchemaFinder(ctx).findRootAndResolver()
    Executor.prepare(
      root, query, ctx,
      deferredResolver=resolver,
      queryReducers=extractors,
    )
  }

  def discoverFromConfig[T]()(implicit ec: ExecutionContext)
      : Future[Seq[T]] = {
    val queryConf = defaultContext.conf.getString("discovery.query")
    val Success(query: Document) = QueryParser.parse(queryConf)
    val extractorsConf = defaultContext.conf
      .getStringList("discovery.extractors").asScala.toList
    val extractors = extractorsConf.map(getObject[Extractor](_))
    val prepared = prepareQuery(query, defaultContext, extractors)
    val result = prepared.flatMap(_.execute())
    if (extractorsConf == List.empty) {
      result.map(_.asInstanceOf[Seq[T]])
    } else {
      prepared.flatMap(p => {
        val extractor = p.userContext.extractor.get
        result.map(r => extractor(r).asInstanceOf[Seq[T]])
      })
    }

  }

  /**
   * Loads the object from fully qualified class name `name`. 
   * 
   * NOTE: The fqcn has to end with a dollar-sign `$` to properly
   * denote an object. We do not try to correct wrong spellings here.
   */
  def getObject[T](name: String): T = {
    val clazz = Class.forName(name).asInstanceOf[Class[_ <: T]]
    val obj = clazz.getDeclaredField("MODULE$")
    obj.get(null).asInstanceOf[T]
  }


}
