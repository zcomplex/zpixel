/*
 * Licensed under ENCRL-1.0 – Non-Commercial Research Use Only
 * See LICENSE file or https://github.com/zcomplex/zpixel.git
 */

package dev.zpixel

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO

type ZPixel = (l: String, r: Int, g: Int, b: Int)

//case class ZFrameDim(width: Int, height: Int)
case class ZFrame(width: Int, height: Int, pixels: Seq[Seq[ZPixel]])

extension (s: String)
  def frameN: String = "\\d+".r
    .findFirstMatchIn(s)
    .map(_.toString)
    .getOrElse("")

private val outPath = "/data/dev/x-pixel/data/out/exp_1/zframes"

@main def run(): Unit =
  val folderPath = "/data/dev/zpixel/data/in/exp_1/frames"
  val pattern = "frame_\\d+\\.png".r

  val files = new File(folderPath)
    .listFiles
    .filter(f => f.isFile && pattern.matches(f.getName))
    .sortBy(_.getName)

  println(s"${files.length} frames found.")

  val frames = files map: file =>

    val img = ImageIO.read(file)
    val width = img.getWidth
    val height = img.getHeight

    val pixels = (0 until height) map: y =>

      val frameN = file.getName.frameN

//      print(s"frame: $frameN") // todo: compute number of fixed digits
      print("reading...")
//      Thread.sleep(2)

      val pxs = (0 until width) map: x =>

        val rgb = img.getRGB(x, y)

        val r = (rgb >> 16) & 0xFF
        val g = (rgb >> 8) & 0xFF
        val b = rgb & 0xFF

//          println(s"($x, $y) = ($r, $g, $b)")
//        println(s"${file.getName.frameN}:$y:$x")
//        print("\r")//\u001b[K")
        print(f"\r\u001b[Kframe: $frameN (\u001b[33m$y\u001b[0mx\u001b[33m$x\u001b[0m)")

//        Thread.sleep(1)

        (s"${file.getName.frameN}:$y:$x", r, g, b)

      println
//      print("\r\u001b[K")
      pxs

    ZFrame(width, height, pixels)

  println(s"frames: ${frames.length}")

  // Sliding
  val newFrames = frames.sliding(2) map:
    case Array(a, b) =>

      val pixels = (0 until a.height) map: y =>
        (0 until a.width) map: x =>

          val aPx = a.pixels(y)(x)
          val bPx = b.pixels(y)(x)

          (
            l = "",
            r = math.abs(bPx.r - aPx.r),
            g = math.abs(bPx.g - aPx.g),
            b = math.abs(bPx.b - aPx.b)
          )

      ZFrame(a.width, a.height, pixels)

  // Drawing frames
  newFrames.zipWithIndex.foreach:
    case (frame, i) =>

      val image = new BufferedImage(frame.width, frame.height, TYPE_INT_RGB)

      (0 until frame.height) map: y =>
        (0 until frame.width) map: x =>

          val pixel = frame.pixels(y)(x)

          val rgb = (pixel.r << 16) | (pixel.g << 8) | pixel.b

          image.setRGB(x, y, rgb)

      ImageIO.write(image, "png", new File(outPath, f"zf_$i%04d.png"))
