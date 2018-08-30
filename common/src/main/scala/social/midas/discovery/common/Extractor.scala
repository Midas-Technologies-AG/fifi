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
package social.midas.discovery.common

import org.apache.logging.log4j.scala.Logging
import sangria.ast.{ Argument => AstArgument, Field => AstField }
import sangria.execution.{ ExecutionPath, QueryReducer }
import sangria.schema.{
  Argument, EnumType, Field, ListType, ObjectType, OptionType, ScalarType,
}
import scala.collection.immutable.ListMap
import scala.util.Try

abstract class Extractor
    extends QueryReducer[AbstractContext, Any]
    with Logging {
  type A
  val provides: Provides

  type Acc = Option[Any => Seq[A]]
  val initial = None

  def sequenceFs(f1: Acc, f2: Acc): Acc = {
    (f1, f2) match {
      case (None, f   ) => f
      case (f,    None) => f
      case (Some(f), Some(g)) => Some(x => f(x) ++ g(x))
    }
  }

  def reduceAlternatives(extractors: Seq[Acc]) =
    extractors.fold(None)(sequenceFs)

  def reduceEnum[T](
    path: ExecutionPath,
    ctx: AbstractContext,
    tpe: EnumType[T],
  ): Acc = 
    initial

  def reduceScalar[T](
    path: ExecutionPath,
    ctx: AbstractContext,
    tpe: ScalarType[T],
  ): Acc = {
    logger.traceEntry(path, ctx, tpe)
    logger.traceExit(
      Some({case x: A @unchecked => Seq(x)})
    )
  }

  def reduceCtx(acc: Acc, ctx: AbstractContext) = {
    val mapInData: Acc = acc.map(f => {
        case x: ListMap[String, Any] @unchecked => f(x("data"))
      })
    ctx.withExtractor(mapInData)
  }

  def reduceField[Val](
    fieldAcc:         Acc,
    childrenAcc:      Acc,
    path:             ExecutionPath,
    ctx:              AbstractContext,
    astFields:        Vector[AstField],
    parentType:       ObjectType[Any,Val],
    field:            Field[AbstractContext,Val],
    argumentValuesFn:
        (ExecutionPath, List[Argument[_]], Vector[AstArgument])
      => Try[sangria.schema.Args],
  ): Acc = {
    logger.traceEntry(
      fieldAcc, childrenAcc, path, ctx, astFields,
      parentType, field, argumentValuesFn,
    )
    val wrapChildren: Acc = childrenAcc.map(f => {
      field.fieldType match {
        case ObjectType(_, _, _, _, _, _, _) =>
          { case x: ListMap[String,Any] @unchecked =>
            f(x(field.name)) }
        case OptionType(_) =>
          { case x: ListMap[String,Any] @unchecked =>
            f(x(field.name)) }
        case ListType(_) =>
          { case x: ListMap[String, Vector[Any]] @unchecked =>
            x(field.name).map(f).toSeq.flatten }
        case _ => f
      }
    })
    val f = sequenceFs(fieldAcc, wrapChildren)
    val here =
      if (field.tags.contains(provides)) {
        val extract: Any => Seq[A] = {
          case x: ListMap[String,A] @unchecked => Seq(x(field.name))
        }
        Some(extract)
      } else {
        initial
      }
    logger.traceExit(sequenceFs(f, here))
  }
}
