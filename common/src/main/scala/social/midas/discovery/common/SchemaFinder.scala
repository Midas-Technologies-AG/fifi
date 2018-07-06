/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

import sangria.execution.deferred.DeferredResolver
import sangria.schema.{ Field, ObjectType, Schema }
import scala.collection.JavaConverters._

final case class SchemaFinder(ctx: AbstractContext) {

  import SchemaFinder._

  def find(): List[AbstractSchema] = {
    val toLoad = ctx.conf.getStringList("discovery.schemata").asScala.toList
    toLoad.map(getObject[AbstractSchema](_))
  }

  def findRootAndResolver(): Tuple2[
    Schema[AbstractContext,Unit],
    DeferredResolver[AbstractContext],
  ] = {
    val schemata = find()
    (makeRoot(schemata), makeResolver(schemata))
  }
}

object SchemaFinder {
  def apply(): SchemaFinder = SchemaFinder(defaultContext)

  def makeRoot(schemata: Seq[AbstractSchema]): Schema[AbstractContext,Unit] = {
    val fields: List[Field[AbstractContext,Unit]] =
      List.concat(schemata.flatMap(_.rootFields))
    val root = ObjectType(
      "RootObject",
      fields,
    )
    Schema(root)
  }

  def makeResolver(schemata: Seq[AbstractSchema]): DeferredResolver[AbstractContext] = {
    val fetchers = Seq.concat(schemata.flatMap(_.fetchers))
    DeferredResolver.fetchers(fetchers: _*)
  }
}
