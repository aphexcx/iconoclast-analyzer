import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.unmarshalling.{FromResponseUnmarshaller, MalformedContent}
import spray.json.DefaultJsonProtocol
import spray.json.lenses.JsonLenses._

import scala.concurrent.Future
import scala.concurrent.duration._

case class FaceDetectResponse(faceId: String, faceRectangle: FaceRectangle, faceAttributes: FaceAttributes)

case class FaceRectangle(top: Int, left: Int, width: Int, height: Int)

case class FaceAttributes(age: Double)

//object FaceDetectResponseJsonProtocol extends DefaultJsonProtocol {
//  implicit val FaceDetectResponseFormat = jsonFormat3(FaceDetectResponse)
//}
//
//object FaceRectangleJsonProtocol extends DefaultJsonProtocol {
//  implicit val FaceRectangleFormat = jsonFormat4(FaceRectangle)
//}
//
//object FaceAttributesJsonProtocol extends DefaultJsonProtocol {
//  implicit val FaceAttributesFormat = jsonFormat1(FaceAttributes)
//}
object FaceAttributesJsonProtocol extends DefaultJsonProtocol {
  implicit val FaceAttributesUnmarshaller = new FromResponseUnmarshaller[FaceAttributes] {
    implicit val FaceAttributesFormat = jsonFormat1(FaceAttributes)

    def apply(response: HttpResponse) = try {
      Right(response.entity.asString.extract[FaceAttributes](element(0) / 'faceAttributes))
    } catch {
      case x: Throwable =>
        Left(MalformedContent("Could not unmarshal user status.", x))
    }
  }
}

import FaceAttributesJsonProtocol._

object MSApi {
  val apiLocation = "https://api.projectoxford.ai/face/v1.0/"
  val timeout = 5.seconds

  //Spray needs an implicit ActorSystem and ExecutionContext
  implicit val system = ActorSystem("apiClient")

  //  import FaceDetectResponseJsonProtocol._
  import system.dispatcher

  val pipeline: HttpRequest => Future[FaceAttributes] = (
    addHeader("Ocp-Apim-Subscription-Key", "03e28955da2c4b7aa48f30527dd275ed")
      ~> sendReceive
      ~> unmarshal[FaceAttributes]
    )

  def estimateAge(ad: Ad): Future[Double] = {
    val ages: List[Future[Double]] = ad.imageUrls.map(getAge(_).map(_.age))

    // Average the ages of the images.
    Future.reduce(ages)(_ + _) map (_ / ad.imageUrls.length)
  }

  //returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=age
  def getAge(imageUrl: String): Future[FaceAttributes] =
    pipeline(Post(s"$apiLocation/detect", FormData(Seq(
      "returnFaceId" -> "true",
      "returnFaceLandmarks" -> "false",
      "returnFaceAttributes" -> "age")
    )))

}

