import spray.json.DefaultJsonProtocol

case class Image(_id: MongoObjectId, url: String, estimatedAge: Double, adId: MongoObjectId)

case class MongoObjectId($oid: String)

object AdJsonProtocol extends DefaultJsonProtocol {
  implicit val mongoOidFormat = jsonFormat1(MongoObjectId)
  implicit val adFormat = jsonFormat4(Image)
}
