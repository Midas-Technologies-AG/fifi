/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas

import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.regions.Region.EU_CENTRAL_1

package object config {
  lazy val conf = ConfigFactory.load()
  lazy val awsRegionString: String = conf.getString("aws.region")
  lazy val awsRegion: Region = Region.of(awsRegionString)
}
