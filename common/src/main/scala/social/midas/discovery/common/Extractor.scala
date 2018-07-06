/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

import sangria.ast.{ Argument => AstArgument, Field => AstField }
import sangria.execution.{ ExecutionPath, QueryReducer }
import sangria.schema.{
  Argument, EnumType, Field, ListType, ObjectType, ScalarType,
}
import scala.collection.immutable.ListMap
import scala.util.Try

abstract class Extractor extends QueryReducer[AbstractContext, Any] {
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
  ): Acc =
    initial

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
    val wrapChildren: Acc = childrenAcc.map(f => {
      field.fieldType match {
        case ObjectType(_, _, _, _, _, _, _) =>
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
    sequenceFs(f, here)
  }
}
