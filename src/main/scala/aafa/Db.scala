package aafa

import java.util.UUID

import scala.collection.concurrent.TrieMap
import scala.util.{Random, Try}

object Db {
  type Key = String

  implicit class ReachTrieMap[B](trie: TrieMap[Key, B]) {
    def add(a: B): (Key, B) = {
      val k: Key = UUID.randomUUID().toString
      trie.put(k, a) // we want to put a value in a non-blocking manner
      (k, a)
    }
  }

  // updated on money transfer
  private val accounts = TrieMap.empty[Key, Account]

  // keep track of all transfers here
  private val transferLog = TrieMap.empty[Key, Transfer]

  def getAccounts: collection.Map[Key, Account] = accounts.readOnlySnapshot()

  def addAccount(account: Account): Try[AccountPayload] = accounts.synchronized {
    Try {
      if (account.amount < 0) throw NegativeAccountAmountNotAllowed
      accounts add account match { case (key: Key, account: Account) => account.asPayload(key) }
    }
  }

  def updateAccount(account: AccountPayload): Unit = accounts.synchronized {
    accounts.update(account.id, account.model)
  }

  def transfer(transferPayload: TransferPayload): Try[TransferResult] = accounts.synchronized {
    val fromId = transferPayload.from
    val toId   = transferPayload.to

    for {
      _ <- Try { if (fromId == toId) throw SelfTransferNotAllowed }
      _ <- Try { if (transferPayload.amount < 0) throw NegativeTransferAmountNotAllowed }

      from <- accounts(fromId).decrease(transferPayload.amount)
      to = accounts(toId).increase(transferPayload.amount)

      _ = accounts.update(fromId, from)
      _ = accounts.update(toId, to)
      _ = transferLog add transferPayload.model

    } yield TransferResult(from.asPayload(fromId), to.asPayload(toId))
  }


  // tests

  def fillWithTestData(): Unit = {
    Db.clearDb()
    accounts put ("0", Account(User("reach guy"), 10000000000L))
    accounts put ("1", Account(User("poor guy"), 10))
    accounts put ("2", Account(User("average guy"), 10000))
  }

  def fillWith10TestAccounts(): List[(Key, Account)] = {
    val maxAccAmount = 9999999

    (1 to 10).toList.map { i =>
      accounts add Account(User(s"test user $i"), Random.nextInt(maxAccAmount))
    }
  }

  def clearDb(): Unit = {
    accounts.clear()
  }
}
