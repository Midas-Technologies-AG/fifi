/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.aws

import org.specs2.execute.{ AsResult, Result }
import org.specs2.specification.ForEach
import software.amazon.awssdk.regions.Region

trait WithAwsContext extends ForEach[AwsContext] {
  def foreach[R: AsResult](f: AwsContext => R): Result = {
    val clients = new AwsClients(Region.EU_CENTRAL_1)
    val ctx: AwsContext = AwsContext(clients)
    try {
      AsResult(f(ctx))
    } finally {
      clients.close()
    }
  }
}
