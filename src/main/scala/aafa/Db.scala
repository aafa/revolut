package aafa

import java.util.UUID

import scala.collection.concurrent.TrieMap
import scala.util.Try

object Db {
  type Key = String

  implicit class ReachTrieMap[B](trie: TrieMap[Key, B]) {
    def add(a: B): (Key, B) = {
      val k : Key = UUID.randomUUID().toString
      trie.put(k, a) // we want to put a value in a non-blocking manner
      (k, a)
    }
  }

  // updated on money transfer
  private val accounts = TrieMap.empty[Key, Account]

  // keep track of all transfers here
  private val transferLog = TrieMap.empty[Key, Transfer]

  def fillWithTestData(): Unit = {
    accounts.clear()
    accounts put ("0", Account(User("reach guy"), 10000000000L))
    accounts put ("1", Account(User("poor guy"), 10))
    accounts put ("2", Account(User("average guy"), 10000))
  }

  def getAccounts: collection.Map[Key, Account] = accounts.readOnlySnapshot()

  def addAccount(account: Account): AccountPayload = {
    (accounts add account) match
    {case (key: Key, account: Account) => account.asPayload(key)}
  }

  def updateAccount(account: AccountPayload): Unit = {
    accounts.update(account.id, account.model)
  }

  def transfer(transferPayload: TransferPayload): Try[TransferResult] = {
    val fromId = transferPayload.from
    val toId   = transferPayload.to
    for { // this `for` scope happen to be quasi-transactional - if not enough funds, no transfer is registered
      from <- accounts(fromId).decrease(transferPayload.amount)
      to = accounts(toId).increase(transferPayload.amount)

      _ = accounts.update(fromId, from)
      _ = accounts.update(toId, to)
      _ = transferLog add transferPayload.model

    } yield TransferResult(from.asPayload(fromId), to.asPayload(toId))
  }
}
