package main

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.io.File

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Graphics2D

case class ServerResponse(serverName: String, image: BufferedImage, endTime: Int, target: ActorRef)

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

			sender() ! ServerResponse("SerialFilter", filteredImage, endTime, self)
		}
		case _ => println("No image provided")
	}
}

class ParallelMedianFilterServer extends Actor {
	def receive = {
		case image: BufferedImage => {
			val startTime = clientServerSimulator.currentTime
			val cores = Runtime.getRuntime.availableProcessors
			val filteredImage =imageFilter.parallelMedianFilter(image,cores)
			val endTime = clientServerSimulator.deltaTime(startTime).toInt

			sender() ! ServerResponse("ParallelFilter-"+cores+"-Cores", filteredImage, endTime, self)
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

		case data: ServerResponse => {
			val filename = "images/processed/"+data.serverName+".png"
			ImageIO.write(data.image, "png" , new File(filename))

			println("Response from: " + data.serverName + "\tTime: "+data.endTime+"\n\tImage Written in: "+filename)
		}

		case _ => println("No image or reply from server")
	}
}

object Main extends App {
	val system = ActorSystem("MedianFilter")
	val image: BufferedImage = ImageIO.read(new File("images/girl.png"))

	//Creating server actors
	val client = system.actorOf(Props[client], name = "client")

	client ! image


}







