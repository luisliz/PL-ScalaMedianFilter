package main

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.io.File

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Graphics2D

object imageFilter {
	def median(list: List[Int]): Int = {
		val seq = list.sortWith(_ < _)

		if (seq.size % 2 == 1) {
			seq(seq.size / 2)
		} else {
			val (up, down) = seq.splitAt(seq.size / 2)
			(up.last + down.head) / 2
		}
	}

	def medianFilter(img: BufferedImage): BufferedImage = {
		// obtain width and height of image
		val w = img.getWidth
		val h = img.getHeight

		// create new image of the same size
		val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
		for (x <- 0 until w)
			for (y <- 0 until h)
				out.setRGB(x, y, img.getRGB(w - x - 1, y) & 0xffffff)

		var size = 3
		var blockSize = List[Int](size * size)
		var count = 0

		for (x <- 1 until w) {
			for (y <- 1 until h) {
				blockSize = List[Int](size * size)

				for (kx <- x - (size) until x + (size))
					for (ky <- y - (size) until y + (size))
						if (!(kx < 0 || kx >= w || ky < 0 || ky >= h)) {
							val p = img.getRGB(kx, ky)
							blockSize = p :: blockSize
						}

				var a = median(blockSize)
				for (kx <- x - (size) until x + (size))
					for (ky <- y - (size) until y + (size))
						if (!(kx < 0 || kx >= w || ky < 0 || ky >= h))
							out.setRGB(kx, ky, a)
			}
		}
		out
	}


	def parallelMedianFilter(img: BufferedImage, cores: Int): BufferedImage = {
		var rows = 1
		var cols = 1

		if(img.getWidth>=img.getHeight)
			cols = cores
		else
			rows = cores




		val chunkWidth = img.getWidth / cols // determines the chunk width and height
		val chunkHeight = img.getHeight / rows

		var imgs = List[BufferedImage]() //Image array to hold image chunks

		for (x <- 0 until rows) {
			for (y <- 0 until cols) {
				val image: BufferedImage = new BufferedImage(chunkWidth, chunkHeight, img.getType())


				val gr: Graphics2D = img.createGraphics()
				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
				gr.dispose();

				imgs = img :: imgs
			}
		}

		val res = imgs.par.map(medianFilter).toList.reverse

		val newImage = new BufferedImage(img.getWidth, img.getHeight, BufferedImage.TYPE_INT_ARGB)
		val g2 = newImage.createGraphics

		var count = 0
		var lastWidth = 0
		var lastHeight = 0
		for(x <- 0 until rows) {
			val nextWidth = lastWidth + res(count).getWidth()
			lastHeight = 0
			for (y <- 0 until cols) {
				val nextHeight = lastHeight + res(count).getHeight()
				g2.drawImage(res(count), null, lastHeight, lastWidth)
				lastHeight = nextHeight
				count += 1
			}
			lastWidth=nextWidth
		}
		newImage
	}
}


object clientServerSimulator {
	def currentTime = System.currentTimeMillis()

	def deltaTime(t0: Long) = currentTime - t0
}

class SerialMedianFilterServer extends Actor {
	def receive = {
		case image: BufferedImage => {
			val startTime = clientServerSimulator.currentTime
			val filteredImage = imageFilter.medianFilter(image)
			val endTime = clientServerSimulator.deltaTime(startTime).toInt

			sender() ! ServerResponse("Parallel Filter", filteredImage, endTime, self)
		}
		case _ => println("No image provided")
	}
}

case class ServerResponse(serverName: String, image: BufferedImage, endTime: Int, target: ActorRef)

class ParallelMedianFilterServer extends Actor {
	def receive = {
		case image: BufferedImage => {
			val startTime = clientServerSimulator.currentTime
			val filteredImage =imageFilter.parallelMedianFilter(image,6)
			val endTime = clientServerSimulator.deltaTime(startTime).toInt

			sender() ! ServerResponse("Parallel Filter", filteredImage, endTime, self)
		}
		case _ => println("No image provided")
	}
}

class client extends Actor {
	def receive = {
		case image: BufferedImage => {
			val serialServerActor = context.actorOf(Props[SerialMedianFilterServer], name = "serialActor")
	 		val parallelServerActor = context.actorOf(Props[ParallelMedianFilterServer], name = "parallelActor")

			serialServerActor ! image
			parallelServerActor ! image
		}

		case data: ServerResponse => println("Response from: " + data.serverName + "\tTime: "+data.endTime+"\n\timage: "+ data.image)

		case _ => println("No image or reply from server")
	}
}

object Main extends App {
	val system = ActorSystem("MedianFilter")
	val image: BufferedImage = ImageIO.read(new File("images/boat.png"))

	//Creating server actors
	val client = system.actorOf(Props[client], name = "client")

	client ! image


}







