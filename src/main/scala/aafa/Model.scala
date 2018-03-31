package aafa

import aafa.Db.Key

import scala.util.{Failure, Success, Try}


// models
sealed trait Model
case class User(name: String) extends Model // gonna add other fields later
case class Account(user: User, amount: Long) extends Model {
  // we will use integer money representation $100.00 is 10000; no currency for now
  override def toString: String =
    """%s has $%d.%d""".format(user.name, amount / 100, amount % 100)
  def asPayload(id: Key): AccountPayload = AccountPayload(user, amount)
}

// payloads
// we dont want to expose our model out there
sealed trait Payload
case class TransferPayload(from: Key, to: Key, amount: Long) extends Payload
case class TransferResult(from: AccountPayload, to: AccountPayload) extends Payload
case class NewAccountPayload(user: User, amount: Long) extends Payload {
  def model: Account = Account(user, amount)
}
case class AccountPayload(user: User, amount: Long) extends Payload {
  def model: Account = Account(user, amount)
}
