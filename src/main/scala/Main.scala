import scala.concurrent.Future

/**
  * Created by aphex on 7/9/16.
  */
class Main extends App {

  def estimateAge(ad: Ad): Future[Double] = (ad.imageUrls
    .map(s => MSApi.getAge(s) map (h => h.faceAttributes.age))
    .sum) / ad.imageUrls.length

  Api.getUnprocessedAd map (ad => ad.copy(estimatedAge = estimateAge(ad))) flatMap (newAd => Api.patchAd(newAd))
}
