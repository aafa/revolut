package aafa

import aafa.Db.Key
import aafa.core.{Service, WireTransfer}
import io.finch._
import io.circe.generic.auto._

import scala.util._
import io.finch.circe._

object Endpoints {
  val accounts: Endpoint[Iterable[AccountPayload]] = get("accounts") {
    Ok(Service.getAccounts.map {
      case (id: Key, account: Account) => account.asPayload(id)
    })
  }

  val account: Endpoint[AccountPayload] = get("account" :: path[Key]) { id: Key =>
    Try(
      Ok(Service.getAccounts(id).asPayload(id))
    ).getOrElse(NotFound(UserNotFound))
  }

  val postAccounts: Endpoint[AccountPayload] =
    post("account" :: jsonBody[NewAccountPayload]) { acc: NewAccountPayload =>
      recover(Service.addAccount(acc.model))
    }

  val transfer: Endpoint[WireTransfer.State] =
    post("transfer" :: jsonBody[TransferPayload]) { tr: TransferPayload =>
      Service.transfer(tr).map(v => Ok(v))
    }

  val all = accounts :+: account :+: postAccounts :+: transfer

  def recover[A](t: Try[A]): Output[A] = t match {
    case Success(v)            => Ok(v)
    case Failure(e: Exception) => BadRequest(e) // to keep unwrapped exception exposed
    case Failure(e)            => BadRequest(new Exception(e))
  }
}
