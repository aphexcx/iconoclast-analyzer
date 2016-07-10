import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._

case class Ad(id: String, url: String, imageUrls: List[String], age: Int, title: String, text: String, estimatedAge: Int)

object AdJsonProtocol extends DefaultJsonProtocol {
  implicit val adFormat = jsonFormat7(Ad)
}

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
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  import system.dispatcher

  val pipelineAd: HttpRequest => Future[Ad] = (
    sendReceive
      ~> unmarshal[Ad]
    )

  import AdJsonProtocol._

  def patchAd(ad: Ad): Future[HttpResponse] = pipeline(Patch(s"$apiLocation/ad/$ad.id"))

  def getUnprocessedAd: Future[Ad] = pipelineAd(Get(s"$apiLocation/ad/unprocessed"))
}
