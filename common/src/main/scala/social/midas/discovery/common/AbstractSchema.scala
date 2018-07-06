/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.discovery.common

import sangria.execution.deferred.Fetcher
import sangria.schema.Field

abstract class AbstractSchema {
  val rootFields: List[Field[AbstractContext,Unit]]
  val fetchers: Seq[Fetcher[AbstractContext, _, _, _]] = Seq.empty
}
