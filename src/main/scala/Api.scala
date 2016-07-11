import AdJsonProtocol._
import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Future
import scala.concurrent.duration._

object Api {
  lazy val IN_DOCKER: Boolean = !System.getProperty("os.name").contains("Mac OS X")
  val apiLocation = if (IN_DOCKER) {
    "http://api:9000/api"
  } else {
    "http://localhost:9000/api"
  }
  val timeout = 5.seconds
  //Spray needs an implicit ActorSystem and ExecutionContext
  implicit val system = ActorSystem("apiClient")

  import system.dispatcher

  val logRequest: HttpRequest => HttpRequest = { r =>
    println(r.toString); r
  }
  val logResponse: HttpResponse => HttpResponse = { r =>
    println(r.toString); r
  }

  val pipeline: HttpRequest => Future[HttpResponse] = (
    logRequest
      ~> sendReceive
      ~> logResponse
    )

  val pipelineToImage: HttpRequest => Future[Image] = (
    logRequest
      ~> sendReceive
      ~> logResponse
      ~> unmarshal[Image]
    )

  def patchImage(image: Image): Future[HttpResponse] = pipeline(Patch(s"$apiLocation/image/${image._id.$oid}", image))

  def getUnprocessedImage: Future[Image] = pipelineToImage(Get(s"$apiLocation/image/unprocessed"))
}