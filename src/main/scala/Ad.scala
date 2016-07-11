import spray.json.DefaultJsonProtocol

case class Ad(_id: MongoObjectId, url: String, imageUrls: List[String], age: Int, title: String, text: String, estimatedAge: Double)

case class MongoObjectId($oid: String)

object AdJsonProtocol extends DefaultJsonProtocol {
  implicit val mongoOidFormat = jsonFormat1(MongoObjectId)
  implicit val adFormat = jsonFormat7(Ad)
}
