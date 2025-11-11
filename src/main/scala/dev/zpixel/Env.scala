/*
 * Licensed under ENCRL-1.0 – Non-Commercial Research Use Only
 * See LICENSE file or https://github.com/zcomplex/zpixel.git
 */

package dev.zpixel

import java.io.File

/** Environment with basic and other useful utils. */
abstract class Env:

  /** Returns experiment key to resolve paths. */
  lazy val experiment: String

  /** Input frames path. */
  val iPath = s"./data/$experiment/i/frames"
  /** Output frames path. */
  val oPath = s"./data/$experiment/o"

  private val iFramesPattern = "(f|frame)_\\d+\\.png".r
  private val progressBarLen = 45

  /** Retrieves frame files for the current experiment. */
  def iframes: Seq[File] = new File(iPath)
    .listFiles
    .filter(f => f.isFile && iFramesPattern.matches(f.getName))
    .sortBy(_.getName)

  /** Prints the progress bar. */
  def progress(s: String, ss: String, done: Int, tot: Int, len: Int = progressBarLen): Unit =
    val percentage = done / tot.toDouble
    val bars = (0 until len)
      .map:
        case n if n <= (percentage * len) => "\u001b[1;33m\u2590\u001b[0m"
        case _ => "\u001b[1;30m\u2590\u001b[0m"
      .mkString

    print(f"\r\u001b[K * $s $bars $ss (${(percentage * 100).ceil.toInt}%03d%%)")