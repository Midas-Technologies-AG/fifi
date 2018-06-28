/**
 * Copyright 2018 Midas Technologies AG
 */
package social

import com.typesafe.config.ConfigFactory

package object midas {

  /**
   * A loaded `ConfigFactory` object. Use this object to load all
   * configuration settings.
   */
  lazy val config = ConfigFactory.load()
}
