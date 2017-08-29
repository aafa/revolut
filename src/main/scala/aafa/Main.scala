package aafa

import com.twitter.finagle.Http.server
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._

object Main extends App {

  Db.fillWithTestData()

  println("started http://localhost:8080")

  Await.ready(
    server
      .withStreaming(enabled = true)
      .serve(":8080",
             Endpoints.all
               .handle {
                 case e: Exception => BadRequest(e)
               }
               .toServiceAs[Application.Json]))

}
