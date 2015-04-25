package com.atomicadd.code

import scala.xml.XML
import java.io.File
import scala.xml.Elem
import scala.collection.mutable

object ViewFieldsGenerator {
  def main(args: Array[String]): Unit = {
    val file = new File("/Users/liuwei/Code/toptal/JoggingTrackerAndroid/app/src/main/res/layout/activity_run.xml")
    val x = XML.loadFile(file)

    val views = mutable.Buffer[(String, String)]()
    def collect(ele: Elem) {
      for (idNodeSeq <- ele.attribute("http://schemas.android.com/apk/res/android", "id"); idValue <- idNodeSeq) {
        val text = idValue.text
        println(text)

        val id = text.substring(text.indexOf('/') + 1)
        val node = (ele.label, id)
        views += node
      }

      for (n <- ele.child) {
        n match {
          case ele: Elem => collect(ele)
          case _         =>
        }
      }
    }

    collect(x)
    println(views)

    val sorted = views.sortBy(_._2)

    for (p <- sorted) {
      p match {
        case (t, v) => println(String.format("private %s %s;", t, v))
      }
    }

    for (p <- sorted) {
      p match {
        case (t, v) => println(String.format("%s = (%s) findViewById(R.id.%s);", v, t, v))
      }
    }

  }
}