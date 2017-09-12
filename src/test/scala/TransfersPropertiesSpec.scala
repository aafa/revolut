import aafa.Db.Key
import aafa.Endpoints.transfer
import aafa._
import io.circe.generic.auto._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop._
import org.scalacheck.{Gen, Properties}
import org.scalatest._
import org.scalatest.prop._
import utils._

import scala.util.Random

class TransfersPropertiesSpec
    extends Properties("transfers")
    with FunSpecLike
    with Matchers
    with Whenever
    with BeforeAndAfter {

  val accountsList: List[(Key, Account)] = Db.fillWith10TestAccounts()
  val accountIds: List[Key] = accountsList.map(_._1)

  val transfers: Gen[TransferPayload] = for {
    a      <- Gen.oneOf[String](accountIds) label "accountId-1"
    b      <- Gen.oneOf[String](accountIds) label "accountId-2"
    amount <- arbitrary[Long] :| "amount"
  } yield TransferPayload(a, b, amount)

  property("bad request on negative transfers") = forAll(transfers suchThat (_.amount < 0)) {
    payload: TransferPayload =>
      val result = transfer.post("/transfer", payload).result
      result.status.code == 400
  }

  property("bad request on amounts greater then people have on their accounts") =
    forAll(transfers suchThat (_.amount > Int.MaxValue)) { payload: TransferPayload =>
      val result = transfer.post("/transfer", payload).result
      result.status.code == 400
    }

  property("all good request") =
    forAll(transfers suchThat(a => a.to != a.from)) { payload: TransferPayload =>
      val result = transfer.post("/transfer", payload.copy(amount = Random.nextInt(9999))).result
      result.status.code == 200
    }

}

