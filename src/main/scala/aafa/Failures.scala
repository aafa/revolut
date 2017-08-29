package aafa

sealed trait FailedResponse extends Exception
case class NotEnoughFunds(requested: Long, actual: Long) extends FailedResponse {
  override def getMessage = s"Amount $requested was requested while only $actual is available"
}

case object UserNotFound extends FailedResponse
