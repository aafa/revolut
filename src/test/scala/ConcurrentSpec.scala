import aafa.Db.Key
import aafa.Endpoints.{transfer, _}
import aafa._
import io.circe.generic.auto._
import io.finch.Input
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import utils._

import scala.collection.immutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

class ConcurrentSpec extends FunSpec with Matchers with BeforeAndAfter{
  val maxTransferAmount = 999999

  Db.clearDb()
  val accountsList: List[(Key, Account)] = Db.fillWith10TestAccounts()
  def accountIds: List[Key] = accountsList.map(_._1)

  private def run(transaction: Int): Unit = {
    val fromAccount = Random.shuffle(accountIds).head
    val toAccount   = Random.shuffle(accountIds).head

    def sign: Int = if (Random.nextBoolean()) -1 else 1
    val r = transfer
      .post("/transfer", TransferPayload(fromAccount, toAccount, sign * Random.nextInt(maxTransferAmount)))
      .result  // here we are expected to have all possible kinds of results - both successful and different failures
//    println(s"[thread ${Thread.currentThread().getName}] running #$transaction with result $r")
  }

  val futuresSeq: immutable.IndexedSeq[Future[Unit]] = {
    for (thread <- 1 to 15)
      yield {
        Future {
          for (transaction <- 1 to 200) run(transaction)
        }
      }
  }

  it("should safely run 15 concurrent threads with 200 transfers each at the same time") {
    val accBefore   = accountsList
    val totalBefore = accBefore.map(_._2.amount).sum
    println(s"accs totals ${accBefore.map(_._2.amount)} and sum ${accBefore.map(_._2.amount).sum}")

    val fResults: Future[immutable.IndexedSeq[Any]] = Future.sequence(futuresSeq)
    Await.ready(fResults, Duration.Inf)

    val accAfter   = accounts(Input.get("/accounts")).value
    val totalAfter = accAfter.map(_.amount).sum
    println(s"accs totals ${accAfter.map(_.amount)} and sum ${accAfter.map(_.amount).sum}")

    totalBefore should equal(totalAfter)
    accAfter.map(_.amount).forall(_ > 0) should be(true)
  }


}
