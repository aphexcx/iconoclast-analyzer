import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by aphex on 7/9/16.
  */
object Main extends App {

  Api.getUnprocessedAd.flatMap(ad =>
    MSApi.estimateAge(ad)
      .map(a => ad.copy(estimatedAge = a))
      .flatMap(Api.patchAd))
    .onComplete(r =>
      println(r)
    )

}
