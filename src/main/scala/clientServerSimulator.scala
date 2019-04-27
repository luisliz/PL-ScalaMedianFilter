package main

import java.io.File

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.awt.{Graphics2D};

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object clientServerSimulator extends App {
	val startTime = currentTime

	// (a) create 2 images
	val serialImage = serialFilterImage("images/girl.png")
	val parallelImage = serialFilterImage("images/girl.png")

	// (b) get a combined result in a for-comprehension
	val result: Future[(BufferedImage, BufferedImage)] = for {
		serialImage <- serialImage
		parallelImage <- parallelImage
	} yield (serialImage, parallelImage)

	// (c) do whatever you need to do with the results
	result.onComplete {
		case Success(x) => {
			val endTime = deltaTime(startTime)
			println(s"In Success case, time delta: ${endTime}")
			println(s"Median Filtered image generated")
		}
		case Failure(e) => e.printStackTrace
	}

	// important for a little parallel demo: need to keep
	// the jvm’s main thread alive
	sleep(5000)

	def sleep(time: Long): Unit = Thread.sleep(time)

	def serialFilterImage(image: String): Future[BufferedImage] = Future {
		val r = scala.util.Random

		val randomSleepTime = r.nextInt(3000)
		println(s"For $image, sleep time is $randomSleepTime")

		// read original image, and obtain width and height
		val imageFilter = new imageFilter
		val photo1 = ImageIO.read(new File(image))
		val photo2 = imageFilter.phototest(photo1)

		// save image to file "test.jpg"
		ImageIO.write(photo2, "png", new File("images/processed/medianFilteredImage.png"))

		sleep(randomSleepTime)
		photo2
	}


	/*def parallelFilteredImage(image: String): Future[BufferedImage] = Future {
		val orig = ImageIO.read(new File(image))

		val division = 2
		val chunkWidth = orig.getWidth() / division // determines the chunk width and height
		val chunkHeight = orig.getHeight() / division;
		var count = 0;


		val imgs = new BufferedImage[Int](division * division)

		for (x <- 0 until division) {
			for (y <- 0 until division) {
				//Initialize the image array with image chunks
				imgs = new BufferedImage(chunkWidth, chunkHeight, BufferedImage.TYPE_INT_RGB) :: imgs

				// draws the image chunk
				count += 1
				var gr: Graphics2D = (imgs(count)).createGraphics()

				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
				gr.dispose();
			}
		}

		/*val futures = Seq[Future[BufferedImage]] = Seq(imgs).map(serialFilterImage)


		a <-
	} yield {*/

	}*/

	def currentTime = System.currentTimeMillis()

	def deltaTime(t0: Long) = currentTime - t0
}