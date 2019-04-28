package main

import java.awt.Graphics2D
import java.awt.image.BufferedImage

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

		//Split by the longest side.
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




