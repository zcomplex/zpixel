/*
 * Licensed under ENCRL-1.0 – Non-Commercial Research Use Only
 * See LICENSE file or https://github.com/zcomplex/zpixel.git
 */
package dev.zpixel

import ai.djl.engine.Engine
import ai.djl.modality.cv.ImageFactory
import ai.djl.ndarray.*
import ai.djl.ndarray.types.{DataType, Shape}

import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO
import scala.collection.mutable.ArrayBuffer

/**
 * Simple program that performs a tomography, it takes in input recorded
 * video frames and computes the Fast Fourier Transform to obtain a representation
 * on the frequency domain of pixels value components in order to potentially study
 * underlying matrix shapes and/or objects.
 */
object ZPixel extends Env with Renderer:

  override lazy val experiment = "exp_0"

  private val Shape1 = Array.fill[Int](1)(1)

  def main(args: Array[String]): Unit =
    val manager = NDManager.newBaseManager()

    println(s" * Selected device: ${manager.getDevice}")
    println(s" * Available engines: ${Engine.getAllEngines}")

    println(s" * ipath: $iPath")
    println(s" * opath: $oPath")

    val files = iframes
  //    .take(5)
  //    .slice(11, 110)
      .zipWithIndex

    val img = ImageIO.read(files.head._1)
    val (width, height) = (img.getWidth, img.getHeight)

    val totFrames = files.length
    val frameSize = width * height
    val totPixels = frameSize * totFrames

    given Context(
      width = width,
      height = height,
      frameSize = frameSize,
      totFrames = totFrames,
      totPixels = totPixels
    )

    val samples = Array.fill(frameSize)(new ArrayBuffer[Array[Number]])

    println(s" * ${files.length} frames found")

    files.foreach:
      case (file, fIdx) =>
        val image = ImageFactory.getInstance.fromFile:
          Paths.get(file.getAbsolutePath)

        val (width, height) = (image.getWidth, image.getHeight)
        val imageArray = image.toNDArray(manager)

        imageArray
          .toType(DataType.FLOAT32, false)
          .div(255)
          .toArray
          .grouped(3)
          .zipWithIndex
          .foreach: (v, i) =>
            samples(i) addOne v

        progress("Sampling", s"${fIdx + 1}", fIdx + 1, totFrames)

    println

    val flatData = samples
      .flatMap: b =>
        b.flatMap: rgb =>
          rgb.map(_.floatValue)

    val data: NDArray = manager.create(flatData, Shape(frameSize, totFrames, 3))

    // spectral analysis, FFT 3-axis (rgb)
    val fftResult = data.fft(3) // complex64 result

    val meanData = fftResult
      .abs()
      .mean(Shape1)
      .toType(DataType.FLOAT32, true)
      .toFloatArray

    val magnitude = fftResult.abs

    val freqIdx = magnitude
      .argMax(1)
      .toType(DataType.FLOAT32, true)

    // maximum intensity (dominant frequency amplitude)
    val maxMag = magnitude.max(Shape1)

    val fftImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

//    amplitudeMean(fftImg, meanData)
//
//    dominantFreq(fftImg):
//      fftResult
//        .abs()
//        .argMax(1)
//        .toType(DataType.FLOAT32, true)
//        .toFloatArray

    heatMap(fftImg):
      (
        freqIdx = freqIdx.toFloatArray,
        maxMag = maxMag.toFloatArray
      )

    ImageIO.write(fftImg, "png", new File(oPath, "fft.png"))

    manager.close()