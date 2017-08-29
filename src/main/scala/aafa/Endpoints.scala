package aafa

import io.finch._
import io.circe.generic.auto._
import scala.util._
import io.finch.circe._

object Endpoints {

  val accounts: Endpoint[Iterable[AccountPayload]] = get("accounts") {
    Ok(Db.getAccounts.map {
      case (id: Int, account: Account) => account.asPayload(id)
    })
  }

  val account: Endpoint[AccountPayload] = get("account" :: path[Int]) { id: Int =>
    Try(
      Ok(Db.getAccounts(id).asPayload(id))
    ).getOrElse(NotFound(UserNotFound))
  }

  val postAccounts: Endpoint[AccountPayload] = post("account" :: jsonBody[NewAccountPayload]) {
    acc: NewAccountPayload =>
      Ok(Db.addAccount(acc.model))
  }

  val transfer: Endpoint[TransferResult] =
    post("transfer" :: jsonBody[TransferPayload]) { tr: TransferPayload =>
      Db.transfer(tr) match {
        case Success(v)            => Ok(v)
        case Failure(e: Exception) => BadRequest(e) // to keep unwrapped exception exposed
        case Failure(e)            => BadRequest(new Exception(e))
      }
    }

  val all = accounts :+: account :+: postAccounts :+: transfer
}
