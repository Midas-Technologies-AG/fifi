/**
 * Copyright 2018 Midas Technologies AG
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
