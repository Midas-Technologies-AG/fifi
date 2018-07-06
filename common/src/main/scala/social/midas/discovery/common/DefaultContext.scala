/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

import com.typesafe.config.ConfigFactory

final case class DefaultContext(
  extractor: Option[Any => Seq[Any]] = None
) extends AbstractContext {
  val conf = ConfigFactory.load()

  def withExtractor(ex: Option[Any => Seq[Any]]): DefaultContext =
    this.copy(extractor=ex)
}
