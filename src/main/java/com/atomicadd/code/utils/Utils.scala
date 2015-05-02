package com.atomicadd.code.utils

import java.io.{File, FileInputStream, InputStream}

/**
 * Created by liuwei on 5/1/15.
 */
object Utils {

  def readAll(stream: InputStream): Array[Byte] = {
    val buffer = new Array[Byte](1024)
    val read = stream.read(buffer)
    if (read < 0)
      Array.emptyByteArray
    else
      buffer.slice(0, read) ++ readAll(stream)
  }

  def readAll(file: File): String = {
    val source = new FileInputStream(file)
    try new String(readAll(source), "utf-8") finally source.close()
  }

}
