package com.atomicadd.code.plugins

import java.io.File

import com.atomicadd.code.{ValueList, ValueString, ValuePair}

import scala.collection.mutable
import scala.xml.{Elem, XML}

object ViewFieldsGenerator {
  def views(file: File) = {
    val x = XML.loadFile(file)

    val views = mutable.Buffer[(String, String)]()
    def collect(ele: Elem) {
      for (idNodeSeq <- ele.attribute("http://schemas.android.com/apk/res/android", "id"); idValue <- idNodeSeq) {
        val text = idValue.text

        val id = text.substring(text.indexOf('/') + 1)
        val node = (ele.label, id)
        views += node
      }

      for (n <- ele.child) {
        n match {
          case ele: Elem => collect(ele)
          case _ =>
        }
      }
    }

    collect(x)

    // sort by name
    val sorted = views.sortBy {
      case (klass, name) => name
    }
    sorted
  }

  def viewsAsValue(file: File) = {
    ValueList(views(file).map {
      case (label, id) => ValuePair(ValueString(label), ValueString(id))
    }.toList)
  }
}