/*
 * Licensed under ENCRL-1.0 – Non-Commercial Research Use Only
 * See LICENSE file or https://github.com/zcomplex/zpixel.git
 */

package dev.zpixel

import java.awt.Color
import java.awt.image.BufferedImage

/** Helpful functions to render data. */
trait Renderer:

  protected def amplitudeMean(img: BufferedImage, data: Array[Float])(using c: Context): Unit =
    for (y <- 0 until c.height; x <- 0 until c.width)
      val i = y * c.width + x
      val r = (data(i * 3 + 0) * 255).toInt.max(0).min(255)
      val g = (data(i * 3 + 1) * 255).toInt.max(0).min(255)
      val b = (data(i * 3 + 2) * 255).toInt.max(0).min(255)
      img.setRGB(x, y, new Color(r, g, b).getRGB)

  protected def dominantFreq(img: BufferedImage)(data: Array[Float])(using c: Context): Unit =
    for (y <- 0 until c.height; x <- 0 until c.width)
      val i = y * c.width + x
      val r = data(i * 3 + 0)
      val g = data(i * 3 + 1)
      val b = data(i * 3 + 2)
      val meanFreq = (r + g + b) / 3f / (c.totFrames / 2f) // normalization 0–1
      val gray = (meanFreq * 255).toInt.max(0).min(255)
      img.setRGB(x, y, new Color(gray, gray, gray).getRGB)

  protected def heatMap(img: BufferedImage)(data: (freqIdx: Array[Float], maxMag: Array[Float]))(using c: Context): Unit =
    val freqArr = data.freqIdx
    val ampArr = data.maxMag

    val freqMax = (c.totFrames / 2).toFloat // half-frequency (FFT symmetry)
    val ampMax = ampArr.max // global normalization

    for (y <- 0 until c.height; x <- 0 until c.width)
      val i = y * c.width + x

      val freqVal = (freqArr(i * 3) + freqArr(i * 3 + 1) + freqArr(i * 3 + 2)) / 3f
      val ampVal = (ampArr(i * 3) + ampArr(i * 3 + 1) + ampArr(i * 3 + 2)) / 3f

      //      val eps = 1e-6f

      val hue = (freqVal / freqMax).min(1f).max(0f) // 0–1
      //      val value = (ampVal / ampMax).min(1f).max(0f) // 0–1
      //      val value = (ampVal / (ampMax + eps)).min(1f).max(0f) // 0–1
      val value = math.log1p(ampVal.toDouble).toFloat / math.log1p(ampMax.toDouble).toFloat
      val color = Color.getHSBColor(hue, 1f, value)

      img.setRGB(x, y, color.getRGB)