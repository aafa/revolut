package aafa.core

import aafa.core.WireTransfer._
import akka.actor.{Actor, ActorRef, FSM}
import akka.event.LoggingReceive
import akka.pattern.AskSupport

object WireTransfer {
  case class Transfer(from: ActorRef, to: ActorRef, amount: BigInt)

  sealed trait State
  object Initial   extends State
  object AwaitFrom extends State
  object AwaitTo   extends State
  object Done      extends State
  object Failed    extends State

  sealed trait Data
  case object UninitializedWireTransfer extends Data
  case class InitialisedWith(from: ActorRef, to: ActorRef, amount: BigInt, client: ActorRef)
      extends Data

}

class WireTransfer extends FSM[State, Data] with AskSupport {

  startWith(Initial, UninitializedWireTransfer)

  when(Initial) {
    case Event(Transfer(from, to, amount), UninitializedWireTransfer) =>
      from ! BankAccount.Withdraw(amount)
      goto(AwaitFrom) using InitialisedWith(from, to, amount, sender())
  }

  when(AwaitFrom) {
    case Event(BankAccount.Done, InitialisedWith(_, to, amount, _)) =>
      to ! BankAccount.Deposit(amount)
      goto(AwaitTo)
    case Event(BankAccount.Failed, InitialisedWith(_, _, _, client)) =>
      client ! Failed
      goto(Done)
      stop()
  }

  when(AwaitTo) {
    case Event(BankAccount.Done, InitialisedWith(_, _, _, client)) =>
      client ! Done
      goto(Done)
      stop()
    case Event(BankAccount.Failed, InitialisedWith(_, _, _, client)) =>
      client ! Failed
      goto(Done)
      stop()
  }

  initialize()
}

object BankAccount {
  case class Deposit(amount: BigInt) // todo refined positive type?
  case class Withdraw(amount: BigInt)
  case object Done
  case object Failed
}

class BankAccount(val id: Long) extends Actor {
  import BankAccount._

  var balance = BigInt(0)

  def receive = LoggingReceive {
    case Deposit(amount) if amount > 0 =>
      balance += amount
      sender ! Done

    case Withdraw(amount) if amount <= balance && amount > 0 =>
      balance -= amount
      sender ! Done

    case _ => sender ! Failed
  }
}
