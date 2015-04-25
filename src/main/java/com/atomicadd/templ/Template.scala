package com.atomicadd.templ

import scala.collection.mutable

trait ValueBase {
}

case class ValueString(str: String) extends ValueBase {}
case class ValueList(list: Seq[ValueBase]) extends ValueBase {}

class Context {
  val varStack = mutable.Buffer[mutable.Map[String, ValueBase]]()
  def push = varStack += mutable.Map[String, ValueBase]()
  def pop = varStack.remove(varStack.size - 1)
  def currentVars = varStack.last

  def apply(name: String) = varStack.reverse.collectFirst {
    case m if (m.contains(name)) => m(name)
  }

  def update(name: String, value: ValueBase) = currentVars(name) = value

  // push default map
  push
}

abstract class TemplateItem() {
  def build(context: Context): String
}

case class StringItem(str: String) extends TemplateItem {
  override def build(context: Context) = str
}

case class VariableItem(name: String) extends TemplateItem {
  override def build(context: Context) = context(name) match {
    case Some(v) => v match {
      case ValueString(s) => s
      case _              => "E_BAD_TYPE"
    }
    case _ => "E_NOT_FOUND"
  }
}

case class ForItem(enName: String, listName: String, internal: TemplateItem) extends TemplateItem {
  override def build(context: Context) = {
    context(listName).orNull match {
      case ValueList(list) => {
        val sb = new StringBuilder()
        for (en <- list) {
          context.push
          context.currentVars(enName) = en
          sb ++= internal.build(context)
          context.pop
        }
        sb.toString()
      }
      case _ => "Invalid forloop, list=" + listName
    }
  }
}

case class ListItem(items: Seq[TemplateItem]) extends TemplateItem {
  override def build(context: Context) = {
    items.map(item => item.build(context)).reduce(_ + _)
  }
}

object Template {
  def getVariables(item: TemplateItem, exclude: Set[String] = Set.empty): Set[String] = {
    def filter(name: String): Set[String] = if (exclude.contains(name)) Set.empty else Set(name)

    item match {
      case VariableItem(name)          => filter(name)
      case ForItem(en, list, internal) => getVariables(internal, exclude + en) ++ filter(list)
      case ListItem(items)             => items.map(getVariables(_, exclude)).reduce(_ ++ _)
      case _                           => Set.empty
    }
  }
}