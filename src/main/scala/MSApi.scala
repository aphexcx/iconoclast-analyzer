import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http._
import spray.httpx.PipelineException
import spray.httpx.unmarshalling.{FromResponseUnmarshaller, MalformedContent}
import spray.json.DefaultJsonProtocol
import spray.json.lenses.JsonLenses._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

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

case class PostParams(url: String)


object FaceAttributesJsonProtocol extends DefaultJsonProtocol {
  implicit val FaceAttributesUnmarshaller = new FromResponseUnmarshaller[FaceAttributes] {
    implicit val FaceAttributesFormat = jsonFormat1(FaceAttributes)

    def apply(response: HttpResponse) = try {
      Right(response.entity.asString.extract[FaceAttributes](element(0) / 'faceAttributes))
    } catch {
      case x: Throwable =>
        Left(MalformedContent("Failed to unmarshal Face Attributes from JSON. Maybe there's no face here?", x))
    }
  }
}

import FaceAttributesJsonProtocol._

object PostParamsJsonProtocol extends DefaultJsonProtocol {
  implicit val PostParamsFormat = jsonFormat1(PostParams)
}


import PostParamsJsonProtocol._
import spray.httpx.SprayJsonSupport._

object MSApi {
  val apiLocation = "https://api.projectoxford.ai/face/v1.0"
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


  val pipeline: HttpRequest => Future[FaceAttributes] = (
    //    addHeader("Content-Type", "application/json")
    addHeader("Ocp-Apim-Subscription-Key", "03e28955da2c4b7aa48f30527dd275ed")
      ~> logRequest

      ~> sendReceive
      ~> logResponse

      ~> unmarshal[FaceAttributes]
    )

  def estimateAge(image: Image): Future[Double] = {
    getAge(image.url).map(_.age).recover { case t: PipelineException => -1.0 }
  }

  //returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=age
  //FormData(Seq(
  //  "returnFaceId" -> "true",
  //  "returnFaceLandmarks" -> "false",
  //  "returnFaceAttributes" -> "age"))
  def getAge(imageUrl: String): Future[FaceAttributes] =
    pipeline(Post(s"$apiLocation/detect"
      + "?returnFaceId=true&returnFaceLandmarks=false&returnFaceAttributes=age",
      PostParams(imageUrl)))


  def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = {
    f.map(Success(_)).recover({ case x => Failure(x) })
  }


}

