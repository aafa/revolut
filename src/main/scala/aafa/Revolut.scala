package aafa

import aafa.Db.accounts
import aafa.Endpoints.transfer
import io.circe.generic.auto._
import io.finch.circe._
import io.finch._
import com.twitter.finagle.Http
import com.twitter.util.Await

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

sealed trait FailedResponse extends Exception
case class NotEnoughFunds(requested: Long, actual: Long) extends FailedResponse {
  override def getMessage = s"Amount $requested was requested while only $actual is available"
}

case object UserNotFound extends FailedResponse

// models
sealed trait Model
case class User(name: String) extends Model // gonna add other fields later
case class Account(user: User, amount: Long) extends Model {
  def decrease(minus: Long): Try[Account] = {
    if (amount - minus > 0)
      Success(this.copy(amount = amount - minus))
    else
      Failure(NotEnoughFunds(minus, amount))
  }

  def increase(plus: Long): Account = {
    this.copy(amount = amount + plus)
  }

  // we will use integer money representation $100.00 is 10000; no currency for now
  override def toString: String =
    """%s has $%d.%d""".format(user.name, amount / 100, amount % 100)
  def asPayload(id: Int): AccountPayload = AccountPayload(id, user, amount)
}
case class Transfer(from: Int, to: Int, amount: Long) extends Model

// payloads
sealed trait Payload
case class TransferPayload(from: Int, to: Int, amount: Long) extends Payload { // from-to an accountId
  def model: Transfer = Transfer(from, to, amount)
}
case class TransferResult(from: AccountPayload, to: AccountPayload) extends Payload
case class AccountPayload(id: Int, user: User, amount: Long) extends Payload {
  def model: Account = Account(user, amount)
}

object Db {
  // updated on money transfer
  private val accounts: mutable.ArrayBuffer[Account] =
    mutable.ArrayBuffer.empty[Account] // index is an account.id

  // keep track of all transfers here
  private val transferLog: mutable.ArrayBuffer[Transfer] =
    mutable.ArrayBuffer.empty[Transfer]

  def fillWithTestData(): Unit = {
    accounts.clear()
    accounts.append(Account(User("reach guy"), 1000000))
    accounts.append(Account(User("poor guy"), 10))
    accounts.append(Account(User("average guy"), 10000))
  }

  def getAccounts: ArrayBuffer[Account] = this.synchronized {
    accounts
  }

  def addAccount(account: Account): Unit = this.synchronized {
    accounts.append(account)
  }

  def updateAccount(account: AccountPayload): Unit = this.synchronized {
    accounts.update(account.id, account.model)
  }

  def transfer(transferPayload: TransferPayload): Try[TransferResult] = this.synchronized {
    val fromId = transferPayload.from
    val toId   = transferPayload.to
    for {
      from <- accounts(fromId).decrease(transferPayload.amount)
      to = accounts(toId).increase(transferPayload.amount)

      _ = accounts.update(fromId, from)
      _ = accounts.update(toId, to)
      _ = transferLog.append(transferPayload.model)

    } yield TransferResult(from.asPayload(fromId), to.asPayload(toId))
  }
}

object Endpoints {

  val accounts: Endpoint[ArrayBuffer[AccountPayload]] = get("accounts") {
    Ok(Db.getAccounts.zipWithIndex.map {
      case (a: Account, id: Int) => a.asPayload(id)
    })
  }

  val account: Endpoint[AccountPayload] = get("account" :: path[Int]) { id: Int =>
    Try(
      Ok(Db.getAccounts(id).asPayload(id))
    ).getOrElse(NotFound(UserNotFound))
  }

  val postAccounts: Endpoint[Unit] = post("account" :: jsonBody[Account]) { acc: Account =>
    Ok(Db.addAccount(acc))
  }

  val transfer: Endpoint[TransferResult] =
    post("transfer" :: jsonBody[TransferPayload]) { tr: TransferPayload =>
      Db.transfer(tr) match {
        case Success(v) => Ok(v)
        case Failure(e) => BadRequest(e.asInstanceOf[Exception])
      }
    }
}

object Revolut extends App {

  Db.fillWithTestData()

  println("started http://localhost:8080")

  import Endpoints._
  Await.ready(
    Http.server.serve(":8080",
                      (
                        accounts :+: account :+: postAccounts :+: transfer
                      ).handle {
                          case e: Exception => BadRequest(e)
                        }
                        .toServiceAs[Application.Json]))

}
