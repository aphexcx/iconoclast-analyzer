import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by aphex on 7/9/16.
  */
object Main extends App {

  val REQUESTS_PER_MINUTE: Int = 20

  while (true) {
    for (i <- 1 to REQUESTS_PER_MINUTE) {
      Api.getUnprocessedImage.flatMap(image =>
        MSApi.estimateAge(image)
          .map(a => image.copy(estimatedAge = a))
          .flatMap(Api.patchImage))
        .onComplete { r =>
          println(r)
          if (i == REQUESTS_PER_MINUTE) println("Sleeping for 1 minute because of rate limiting...")
        }
    }
    Thread.sleep(62 * 1000)
  }

}
