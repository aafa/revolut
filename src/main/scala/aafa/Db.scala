package aafa

import scala.collection.concurrent.TrieMap
import scala.util.Try

object Db {
  implicit class ReachTrieMap[B](trie: TrieMap[Int, B]) {
    def add(a: B): Option[B] = trie.put(trie.size, a)
  }

  // updated on money transfer
  private val accounts = TrieMap.empty[Int, Account]

  // keep track of all transfers here
  private val transferLog = TrieMap.empty[Int, Transfer]

  def fillWithTestData(): Unit = {
    accounts.clear()
    accounts add Account(User("reach guy"), 10000000000L)
    accounts add Account(User("poor guy"), 10)
    accounts add Account(User("average guy"), 10000)
  }

  def getAccounts: collection.Map[Int, Account] = accounts.readOnlySnapshot()

  def addAccount(account: Account): AccountPayload = {
    accounts add account
    account.asPayload(accounts.size - 1) // performance!
  }

  def updateAccount(account: AccountPayload): Unit = {
    accounts.update(account.id, account.model)
  }

  def transfer(transferPayload: TransferPayload): Try[TransferResult] = {
    val fromId = transferPayload.from
    val toId   = transferPayload.to
    for {
      from <- accounts(fromId).decrease(transferPayload.amount)
      to = accounts(toId).increase(transferPayload.amount)

      _ = accounts.update(fromId, from)
      _ = accounts.update(toId, to)
      _ = transferLog add transferPayload.model

    } yield TransferResult(from.asPayload(fromId), to.asPayload(toId))
  }
}
