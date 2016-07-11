import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by aphex on 7/9/16.
  */
object Main extends App {

  for (i <- 1 to 10) {
    Api.getUnprocessedImage.flatMap(image =>
      MSApi.estimateAge(image)
        .map(a => image.copy(estimatedAge = a))
        .flatMap(Api.patchImage))
      .onComplete(r =>
        println(r)
      )
  }

}
