import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color

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

	def phototest(img: BufferedImage): BufferedImage = {
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

	def test() {
		// read original image, and obtain width and height
		val photo1 = ImageIO.read(new File("images/color.jpg"))
		val photo2 = phototest(photo1)

		// save image to file "test.jpg"
		ImageIO.write(photo2, "png", new File("images/processed/test.png"))
	}

	def main(args: Array[String]): Unit = {
		test()
	}
}



