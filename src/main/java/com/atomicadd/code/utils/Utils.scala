package com.atomicadd.code.utils

import java.io.File

/**
 * Created by liuwei on 5/1/15.
 */
object Utils {

  def readAll(file:File) = {
    val source = scala.io.Source.fromFile(file)
    try source.getLines().mkString("\n") finally source.close()
  }
}
