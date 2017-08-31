package aafa

import aafa.Db.Key

import scala.util.{Failure, Success, Try}


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
  def asPayload(id: Key): AccountPayload = AccountPayload(id, user, amount)
}
case class Transfer(from: Key, to: Key, amount: Long) extends Model

// payloads
// we dont want to expose our model out there
sealed trait Payload
case class TransferPayload(from: Key, to: Key, amount: Long) extends Payload { // from-to an accountId
  def model: Transfer = Transfer(from, to, amount)
}
case class TransferResult(from: AccountPayload, to: AccountPayload) extends Payload
case class NewAccountPayload(user: User, amount: Long) extends Payload {
  def model: Account = Account(user, amount)
}
case class AccountPayload(id: Key, user: User, amount: Long) extends Payload {
  def model: Account = Account(user, amount)
}
