/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

object Ip4Extractor extends Extractor {
  type A = String
  val provides = Provides("ip4")
}
