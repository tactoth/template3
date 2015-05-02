package com.atomicadd.code

import java.io.File

import com.atomicadd.code.parse._
import com.atomicadd.code.utils.Utils

/**
 * Created by liuwei on 5/1/15.
 */
object Template {

  // constructors
  def apply(templateStr: String) = new Template(Striper.strip(templateStr))

  def apply(file: File): Template = apply(Utils.readAll(file))

  def getVariables(item: TemplateItem, exclude: Set[String] = Set.empty): Set[String] = {
    def filter(name: String): Set[String] = if (exclude.contains(name)) Set.empty else Set(name)

    item match {
      case VariableItem(name) => filter(name)
      case ForItem(en, list, internal) => getVariables(internal,
        exclude + en + (en + ".first") + (en + ".second")) ++ filter(list)
      case ListItem(items) => items.map(getVariables(_, exclude)).reduce(_ ++ _)
      case _ => Set.empty
    }
  }
}

class Template(root: TemplateItem) {
  def getVariables = Template.getVariables(root)
}