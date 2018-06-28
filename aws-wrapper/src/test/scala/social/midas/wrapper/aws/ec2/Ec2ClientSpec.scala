/**
 * Copyright 2018 Midas Technologies AG
 */
package social.midas.wrapper.aws.ec2

import cats.effect.IO
import org.specs2.execute.{ AsResult, Result }
import org.specs2.mutable.Specification
import org.specs2.specification.ForEach
import software.amazon.awssdk.regions.Region

trait WithEc2Client extends ForEach[Ec2Client] {
  def foreach[R: AsResult](f: Ec2Client => R): Result = {
    val client = Ec2Client(Region.EU_CENTRAL_1)
    try {
      AsResult(f(client))
    } finally {
      client.close()
    }
  }
} 

class Ec2ClientSpec extends Specification with WithEc2Client {
  "describeInstances" >> {
    "yields at least one result" >> { client: Ec2Client =>
      val result = client.describeInstances().unsafeRunSync()
      result must not be empty
    }

    "filtering works" >> { client: Ec2Client =>
      val prog = for {
        existing <- client.describeInstances()
        instance <- IO.pure(existing.head.id)
        selected <- client.describeInstances(Seq(instance))
      } yield (existing, selected)
      val (existing, selected) = prog.unsafeRunSync()

      selected must have size(1)
      selected.head must_=== existing.head
    }

    "online filter" >> { client: Ec2Client =>
      val prog = client.describeInstancesByReservation(
        filters = Map("tag:Name" -> Seq("non-existent"))
      )
      val reservations = prog.unsafeRunSync()
      reservations must be empty
    }
  }
}
