import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol

import scala.concurrent.Future
import scala.concurrent.duration._

case class FaceDetectResponse(faceId: String, faceRectangle: FaceRectangle, faceAttributes: FaceAttributes)

case class FaceRectangle(top: Int, left: Int, width: Int, height: Int)

case class FaceAttributes(age: Double)

object FaceDetectResponseJsonProtocol extends DefaultJsonProtocol {
  implicit val FaceDetectResponseFormat = jsonFormat3(FaceDetectResponse)
}

object FaceRectangleJsonProtocol extends DefaultJsonProtocol {
  implicit val FaceRectangleFormat = jsonFormat4(FaceRectangle)
}

object FaceAttributesJsonProtocol extends DefaultJsonProtocol {
  implicit val FaceAttributesFormat = jsonFormat1(FaceAttributes)
}

object MSApi {
  val apiLocation = "https://api.projectoxford.ai/face/v1.0/"
  val timeout = 5.seconds

  //Spray needs an implicit ActorSystem and ExecutionContext
  implicit val system = ActorSystem("apiClient")

  import FaceDetectResponseJsonProtocol._
  import system.dispatcher

  val pipeline: HttpRequest => Future[FaceDetectResponse] = (
    addHeader("Ocp-Apim-Subscription-Key", "03e28955da2c4b7aa48f30527dd275ed")
      ~> sendReceive
      ~> unmarshal[FaceDetectResponse]
    )

  //returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=age
  def getAge(imageUrl: String): Future[FaceDetectResponse] =
    pipeline(Post(s"$apiLocation/detect", FormData(Seq(
      "returnFaceId" -> "true",
      "returnFaceLandmarks" -> "false",
      "returnFaceAttributes" -> "age")
    )))

}

