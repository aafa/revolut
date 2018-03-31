import io.circe.Encoder
import io.circe.syntax._
import io.finch.Endpoint.Result
import io.finch.Text.Plain
import io.finch.{Endpoint, Input, Output}

package object aafa {

  implicit class ReachEndpoint[A](e: Endpoint[A]){
    def post[P <: Payload](path: String, payload: P)(implicit encoder: Encoder[P]): Result[A] = {
      e(Input.post(path).withBody[Plain](payload.asJson))
    }
  }


  implicit class TestEndpoint[A](e: Endpoint.Result[A]) {
    def result: Output[A] = e.awaitOutputUnsafe().get
    def value: A          = e.awaitOutputUnsafe().get.value
  }

}
