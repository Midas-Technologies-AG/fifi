/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

import software.amazon.awssdk.regions.Region

/**
 * Common abstractions used in other parts of the code.
 */
package object aws {
  def regionFromContext(ctx: AbstractContext): Region = {
    val fromConf = ctx.conf.getString("discovery.aws.region")
    Region.of(fromConf)
  }

  def region(): Region = regionFromContext(defaultContext)
}
