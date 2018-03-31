package aafa.core

import aafa.Db._
import aafa._
import aafa.core.WireTransfer.Transfer
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.twitter.util.Future

import scala.concurrent.duration._
import scala.util.Try

object Service {
  private val ac                = ActorSystem("awesome_bank")
  implicit val timeout: Timeout = Timeout(5 seconds)

  def getAccounts: collection.Map[Key, Account] = Db.accounts.readOnlySnapshot()

  def addAccount(account: Account): Try[AccountPayload] = {
    Try {
      if (account.amount < 0) throw NegativeAccountAmountNotAllowed
      accounts add account match { case (key: Key, account: Account) => account.asPayload(key) }
    }
  }

  def transfer(transferPayload: TransferPayload): Future[WireTransfer.State] = {
    val from: ActorRef =
      ac.actorOf(Props(classOf[BankAccount], accounts(transferPayload.from).amount))
    val to: ActorRef = ac.actorOf(Props(classOf[BankAccount], accounts(transferPayload.to).amount))

    val transfer = ac.actorOf(Props[WireTransfer])

    (transfer ? Transfer(from, to, transferPayload.amount)).asInstanceOf

    // todo transferLog add

  }
}
