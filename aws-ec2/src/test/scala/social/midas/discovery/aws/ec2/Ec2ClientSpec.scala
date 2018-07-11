/**
 * Copyright 2018 Midas Technologies AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package social.midas.discovery.aws.ec2

import cats.effect.IO
import org.specs2.mutable.Specification
import software.amazon.awssdk.regions.Region

class Ec2ClientSpec extends Specification {
  val client = Ec2Client(Region.EU_CENTRAL_1)
  "describeInstances" >> {
    "yields at least one result" >> { 
      val result = client.describeInstances().unsafeRunSync()
      result must not be empty
    }

    "filtering works" >> {
      val prog = for {
        existing <- client.describeInstances()
        instance <- IO.pure(existing.head.id)
        selected <- client.describeInstances(Seq(instance))
      } yield (existing, selected)
      val (existing, selected) = prog.unsafeRunSync()

      selected must have size(1)
      selected.head must_=== existing.head
    }

    "online filter" >> {
      val prog = client.describeInstancesByReservation(
        filters = Seq(Ec2Filter("tag:Name", Seq("non-existent")))
      )
      val reservations = prog.unsafeRunSync()
      reservations must be empty
    }
  }
}
