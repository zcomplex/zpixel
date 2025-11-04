/*
 * Licensed under ENCRL-1.0 – Non-Commercial Research Use Only
 * See LICENSE file or https://github.com/zcomplex/zpixel.git
 */
package dev.zpixel

import dev.zpixel.generated.BuildInfo.{Name, Version}

import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import javax.imageio.ImageIO

type ZRgb = (r: Int, g: Int, b: Int)
type ZPixel = Int

case class ZFrame(width: Int, height: Int, pixels: Seq[ZPixel])

extension (s: String)
  def frameN: String = "\\d+".r
    .findFirstMatchIn(s)
    .map(_.toString)
    .getOrElse("")

extension (f: File)
  def zframe: ZFrame =
    val img    = ImageIO.read(f)
    val (w, h) = (img.getWidth, img.getHeight)

    val pixels = new Array[ZPixel](w * h)

    // initializing pixels
    pixels.indices foreach: i =>
      // converting index to bidimensional coords
      val (x, y) = (i % w, i / w)
      // reading pixel rgb value at (x, y)
      pixels(i) = img.getRGB(x, y)

    ZFrame(w, h, pixels)

extension (i: Int)
  def rgb: ZRgb = (r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF)

extension (rgb: ZRgb)
  def int: Int = (rgb.r << 16) | (rgb.g << 8) | rgb.b

extension (zpx: ZPixel)
  def brightness: Int =
    val rgb = zpx.rgb
    (0.2126 * rgb.r + 0.7152 * rgb.g + 0.0722 * rgb.b).round.toInt

private val outPath = "/data/dev/zpixel/data/out/exp_1/zframes"

@main def run(): Unit =

  val termCols = 80

  val buildInfo = s"\u001b[1;97m$Name\u001b[0m $Version"
  val buildFrame = (0 until termCols).map(_ => "=").mkString

  println(buildInfo)
  println(buildFrame)

  val folderPath = "/data/dev/zpixel/data/in/exp_1/frames"
  val pattern = "frame_\\d+\\.png".r

  val files = new File(folderPath)
    .listFiles
    .filter(f => f.isFile && pattern.matches(f.getName))
    .sortBy(_.getName)
    .zipWithIndex

  println(s" * ${files.length} frames found.")

  if files.isEmpty then
    println(" * no file to process")
    sys.exit(0)

  val (file0, _) = files.head

  val img = ImageIO.read(file0)
  val (width, height) = (img.getWidth, img.getHeight)

  // caching useful data
  val iBuff = new BufferedImage(width, height, TYPE_INT_RGB)
  val pbLen = 45 // progress bar length
  
  // processing frames
  files.sliding(2) foreach:
    case Array((aF, aI), (bF, bI)) =>

      val perc = bI / (files.length.toDouble - 1)
      val bars = (0 until pbLen)
        .map:
          case n if n <= (perc * pbLen) => "\u001b[1;33m\u2590\u001b[0m"
          case _ => "\u001b[1;30m\u2590\u001b[0m"
        .mkString

      print(f"\r\u001b[K * Processing $bars ${aI + 1}%06d-${bI + 1}%06d (${(perc * 100).ceil.toInt}%03d%%)")
      val (frameL, frameR) = (aF.zframe, bF.zframe)

      // processing matrix
      (0 until width * height) foreach: i =>

        // current pixel(s)
        val (x, y) = (i % width, i / width)
        val (pxL, pxR) = (frameL.pixels(i), frameR.pixels(i))

        val (rgbL, rgbR) = (pxL.rgb, pxR.rgb)

        val rgb = (
          r = math.abs(rgbL.r - rgbR.g),
          g = math.abs(rgbL.g - rgbR.b),
          b = math.abs(rgbL.b - rgbR.r)
        )

        iBuff.setRGB(x, y, rgb.int)

      ImageIO.write(iBuff, "png", new File(outPath, f"zf_$aI%04d.png"))
      iBuff.flush()

  println("\n * done.")
