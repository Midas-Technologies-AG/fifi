/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

import com.typesafe.config.Config

abstract class AbstractContext {
  val conf: Config
  val extractor: Option[Any => Seq[Any]]
  def withExtractor(ex: Option[Any => Seq[Any]]): AbstractContext
}
