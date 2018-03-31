package aafa

import java.util.UUID

import aafa.core.WireTransfer.Transfer
import aafa.core._
import akka.actor._
import com.twitter.util.Future

import scala.collection.concurrent.TrieMap
import scala.util.{Random, Try}

object Db {
  type Key = String

  implicit class ReachTrieMap[B](trie: TrieMap[Key, B]) {
    def add(a: B): (Key, B) = {
      val k: Key = UUID.randomUUID().toString
      trie.put(k, a) // we want to put in value in a non-blocking manner
      (k, a)
    }
  }

  // updated on money transfer
  val accounts: TrieMap[Key, Account] = TrieMap.empty[Key, Account]

  // keep track of all transfers here
  val transferLog: TrieMap[Key, Transfer] = TrieMap.empty[Key, Transfer]

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
