import aafa._
import com.twitter.finagle.http.Status
import io.finch.{Application, Endpoint, Input}
import org.scalatest.{FunSpec, MustMatchers}
import io.finch._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

class ApiSpec extends FunSpec with MustMatchers {

  implicit class TestEndpoint[A](e: Endpoint.Result[A]) {
    def result: Output[A] = e.awaitOutputUnsafe().get
    def value: A          = e.awaitOutputUnsafe().get.value
  }

  import aafa.Endpoints._

  describe("should show some data") {
    Db.fillWithTestData()

    it("should return some accounts") {
      val payload = ArrayBuffer(AccountPayload(0, User("reach guy"), 10000000000L),
                                AccountPayload(1, User("poor guy"), 10),
                                AccountPayload(2, User("average guy"), 10000))
      accounts(Input.get("/accounts")).value must ===(payload)
    }

    it("should do transfers") {
      val payload = TransferPayload(0, 1, 10)
      transfer(Input.post("/transfer").withBody[Text.Plain](payload.asJson)).value must ===(
        TransferResult(AccountPayload(0, User("reach guy"), 10000000000L - 10),
                       AccountPayload(1, User("poor guy"), 20)))


      account(Input.get("/account/0")).value must === (AccountPayload(0, User("reach guy"), 10000000000L - 10))
      account(Input.get("/account/1")).value must === (AccountPayload(1, User("poor guy"), 20))
    }

    it("should fail if account not found"){
      account(Input.get("/account/5")).result.status must === (Status(404))
      account(Input.get("/account/5")).result must === (NotFound(UserNotFound))
    }

    it("should fail if not enough funds (payback time)") {
      val payload = TransferPayload(1, 0, 30)
      transfer(Input.post("/transfer").withBody[Text.Plain](payload.asJson)).result must ===(
        BadRequest(NotEnoughFunds(30,20))
      )
    }

    it ("should add new accounts"){
      val p = NewAccountPayload(User("Nouveau riche"), 9999999999L)
      val expectedResult = AccountPayload(3, p.user, p.amount)

      postAccounts(Input.post("/account").withBody[Text.Plain](p.asJson)).value must === (expectedResult)
      account(Input.get(s"/account/${expectedResult.id}")).value must === (expectedResult)
      accounts(Input.get("/accounts")).value.last must ===(expectedResult)
    }
  }

}
