import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color

object imageFilter {
    def median(list: List[Int]): Int= {
        val seq = list.sortWith(_<_)
        if(seq.size % 2 == 1) {
            seq(seq.size / 2)
        } else {
            val (up, down) = seq.splitAt (seq.size / 2)
            (up.last + down.head) / 2
        }
    }

    def phototest(img: BufferedImage): BufferedImage = {
        // obtain width and height of image
        val w = img.getWidth
        val h = img.getHeight

        // create new image of the same size
        val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        for(x <- 0 until w)
            for(y <- 0 until h)
                out.setRGB(x, y, img.getRGB(w - x - 1, y) & 0xffffff)

        // copy pixels (mirror horizontally)
        for(xblock <- 0 until w by 3) {
            for(yblock <- 0 until h by 3) {
                var sumR = List[Int]()
                var sumG = List[Int]()
                var sumB = List[Int]()
                var sumA = List[Int]()
//                out.setRGB(xblock, yblock, 0xff0000)
                for(x <- 0 to xblock) {
                    for(y <- 0 to yblock) {
                        val p = img.getRGB(x, y)

                        //get alpha
                        val a = (p >> 24) & 0xff
                        //get red
                        val r = (p >> 16) & 0xff
                        //get green
                        val g = (p >> 8) & 0xff
                        //get blue
                        val b = p & 0xff

                        sumR = r :: sumR
                        sumG = g :: sumG
                        sumB = b :: sumB
                        sumA = a :: sumA
                    }
                }
                val r = median(sumR)
                val g = median(sumG)
                val b = median(sumB)
                val a = median(sumA)

                println(r + ","+g+","+b+","+a)
                /*for(x <- 0 to xblock) {
                    for(y <- 0 to yblock) {
                        var rgb = new Color(a,r,g,b).getRGB();

                        out.setRGB(x,y,rgb)

                    }
                }*/
            }
        }
        /* for(x <- 0 until w by 3) {
             for(y <- 0 until h by 3) {
                 kkkval p = img.getRGB(x, y)

                 //get alpha
                 val a = (p >> 24) & 0xff

                 //get red
                 val r = (p >> 16) & 0xff

                 //get green
                 val g = (p >> 8) & 0xff

                 //get blue
                 val b = p & 0xff

                 print(r + "," + g + "," + b + "-" + a + "|")
             }
             println()
         }*/

        //out.setRGB(x, y, img.getRGB(w - x - 1, y) & 0xffffff)


        /*i = 0
        for fx from 0 to window width
            for fy from 0 to window height
                window[i] := inputpixelvalue[x + fx - edgex][y + fy - edgey]
                i := i + 1
            sort entries in window[]
            outputpixelvalue[x][y] := window[window width * window height / 2]*/

        out
    }

    def test() {
        // read original image, and obtain width and height
        val photo1 = ImageIO.read(new File("images/boat.png"))
        val photo2 = phototest(photo1)

        // save image to file "test.jpg"
        ImageIO.write(photo2, "png", new File("images/processed/test.png"))
    }

    def main(args: Array[String]): Unit = {
        test()
    }
}