/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import org.apache.logging.log4j.scala.Logging
import sangria.ast.{ Argument => AstArgument, Field => AstField }
import sangria.execution.{ ExecutionPath, QueryReducer }
import sangria.schema.{
  Argument, EnumType, Field, ListType, ObjectType, ScalarType,
}
import scala.collection.immutable.ListMap
import scala.util.Try

case class AwsContext(
  clients: AwsClients,
  extractor: Option[Any => Seq[String]] = None,
) extends ExtractContext[String] {
  def withExtractor(extractor: Option[Any => Seq[String]]) =
    this.copy(extractor=extractor)
}

trait ExtractContext[A] {
  val extractor: Option[Any => Seq[A]]
  def withExtractor(extractor: Option[Any => Seq[A]]): ExtractContext[A]
}

object Extractors {
  val ip4Extractor: QueryReducer[AwsContext, Any] =
    new Extractor[String]
}

class Extractor[A] extends QueryReducer[ExtractContext[A], Any]
    with Logging
{
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
    ctx: ExtractContext[A],
    tpe: EnumType[T],
  ): Acc =
    initial

  def reduceScalar[T](
    path: ExecutionPath,
    ctx: ExtractContext[A],
    tpe: ScalarType[T],
  ): Acc =
    initial

  def reduceCtx(acc: Acc, ctx: ExtractContext[A]) = {
    val mapInData: Acc = acc.map(f => {
        case x: ListMap[String, Any] => f(x("data"))
      })
    ctx.withExtractor(mapInData)
  }

  def reduceField[Val](
    fieldAcc:         Acc,
    childrenAcc:      Acc,
    path:             ExecutionPath,
    ctx:              ExtractContext[A],
    astFields:        Vector[AstField],
    parentType:       ObjectType[Any,Val],
    field:            Field[ExtractContext[A],Val],
    argumentValuesFn:
        (ExecutionPath, List[Argument[_]], Vector[AstArgument])
      => Try[sangria.schema.Args],
  ): Acc = {
    logger.traceEntry(Seq(
      s"fieldAcc:         ${fieldAcc}",
      s"childrenAcc:      ${childrenAcc}",
      s"path:             ${path}",
      s"ctx:              ${ctx}",
      s"astFields:        ${astFields}",
      s"parentType:       ${parentType}",
      s"field:            ${field}",
      s"argumentValuesFn: ${argumentValuesFn}",
    ).mkString("\n"))

    val wrapChildren: Acc = childrenAcc.map(f => {
      field.fieldType match {
        case ObjectType(_, _, _, _, _, _, _) =>
          { case x: ListMap[String,Any] =>
            f(x(field.name)) }
        case ListType(_) =>
          { case x: ListMap[String, Vector[Any]] =>
            x(field.name).map(f).toSeq.flatten }
        case _ => f
      }
    })
    val f = sequenceFs(fieldAcc, wrapChildren)
    val here =
      if (field.tags.contains(Provides("ip4"))) {
        val extract: Any => Seq[A] = {
          case x: ListMap[String,A] => Seq(x(field.name))
        }
        Some(extract)
      } else {
        initial
      }
    val result = sequenceFs(f, here)
    logger.traceExit(s"${result}")
    result
  }
}
