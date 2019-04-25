package futures

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object clientServerSimulator extends App {
    val startTime = currentTime

    // (a) create 2 images
    val serialImage = getFilteredImage("images/boat.png")
    val parallelImage = getFilteredImage("images/boat.png")

    // (b) get a combined result in a for-comprehension
    val result: Future[(Double, Double)] = for {
        serialImage <- serialImage
        parallelImage <- parallelImage
    } yield (serialImage, parallelImage )

    // (c) do whatever you need to do with the results
    result.onComplete {
        case Success(x) => {
            val endTime = deltaTime(startTime)
            println(s"In Success case, time delta: ${endTime}")
            println(s"The stock prices are: $x")
        }
        case Failure(e) => e.printStackTrace
    }

    // important for a little parallel demo: need to keep
    // the jvm’s main thread alive
    sleep(5000)

    def sleep(time: Long): Unit = Thread.sleep(time)

    // a simulated web service
    def getFilteredImage(image: String): Future[Double] = Future {
        val r = scala.util.Random
        val randomSleepTime = r.nextInt(3000)
        println(s"For $image, sleep time is $randomSleepTime")
        val randomPrice = r.nextDouble * 1000
        sleep(randomSleepTime)
        randomPrice
    }

    def currentTime = System.currentTimeMillis()

    def deltaTime(t0: Long) = currentTime - t0
}