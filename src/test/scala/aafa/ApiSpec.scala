package aafa

import io.finch.Input
import org.scalatest.{BeforeAndAfter, FunSpec, MustMatchers}

import scala.collection.mutable.ArrayBuffer

class ApiSpec extends FunSpec with MustMatchers with BeforeAndAfter {

  import aafa.Endpoints._

  describe("should show some data") {
    Db.fillWithTestData()

    val reachGuy: AccountPayload = accounts(Input.get("/accounts")).value.find(_.user.name == "reach guy").get
    val poorGuy: AccountPayload = accounts(Input.get("/accounts")).value.find(_.user.name == "poor guy").get

    it("should return some accounts") {
      val payload = ArrayBuffer(
        Account(User("poor guy"), 10),
        Account(User("average guy"), 10000),
        Account(User("reach guy"), 10000000000L),
      )

      val accounts1 = accounts(Input.get("/accounts")).value.map(_.model).toVector.sortBy(_.amount)
      accounts1.filter(_.user.name == "poor guy") must ===(payload.filter(_.user.name == "poor guy"))
      accounts1.filter(_.user.name == "average guy") must ===(payload.filter(_.user.name == "average guy"))
    }

//    it("should do transfers") {
//      val payload = TransferPayload(reachGuy.id, poorGuy.id, 10)
//      val trRes   = transfer(Input.post("/transfer").withBody[Plain](payload.asJson)).value
//
//      trRes.from.model must ===(Account(User("reach guy"), 10000000000L - 10))
//      trRes.to.model must ===(Account(User("poor guy"), 20))
//
//      account(Input.get(s"/account/${reachGuy.id}")).value.model must ===(
//        Account(User("reach guy"), 10000000000L - 10))
//      account(Input.get(s"/account/${poorGuy.id}")).value.model must ===(
//        Account(User("poor guy"), 20))
//    }
//
//    it("should fail if account not found") {
//      account(Input.get("/account/5")).result.status must ===(Status(404))
//      account(Input.get("/account/5")).result must ===(NotFound(UserNotFound))
//    }
//
//    it("should fail if not enough funds (payback time)") {
//      val payload = TransferPayload(poorGuy.id, reachGuy.id, 30)
//      transfer(Input.post("/transfer").withBody[Text.Plain](payload.asJson)).result must ===(
//        BadRequest(NotEnoughFunds(30, 20))
//      )
//    }
//
//    it("should add new accounts") {
//      val p              = NewAccountPayload(User("Nouveau riche"), 9999999999L)
//      val expectedResult = Account(p.user, p.amount)
//
//      val accountPayload = postAccounts.post("/account", p).value
//
//      accountPayload.model must ===(expectedResult)
//      account(Input.get(s"/account/${accountPayload.id}")).value.model must ===(expectedResult)
//      accounts(Input.get("/accounts")).value
//        .find(_.user.name == "Nouveau riche")
//        .map(_.model).get must ===(expectedResult)
//    }

  }

}
