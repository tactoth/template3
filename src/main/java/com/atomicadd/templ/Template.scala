package com.atomicadd.templ

import scala.collection.mutable

trait ValueBase {
}

case class ValueString(str: String) extends ValueBase {}

case class ValuePair(first: ValueBase, second: ValueBase) extends ValueBase {}

case class ValueList(list: Seq[ValueBase]) extends ValueBase {}

class Context {

  // managing variables
  private val varStack = mutable.Buffer[mutable.Map[String, ValueBase]]()

  private def currentVars = varStack.last

  def push = varStack += mutable.Map[String, ValueBase]()

  def pop = varStack.remove(varStack.size - 1)

  def apply(name: String) = varStack.reverse.collectFirst {
    // loves partial function
    case m if m.contains(name) => m(name)
  }

  def update(name: String, value: ValueBase) = currentVars(name) = value

  // push default map
  push

  // plugins for users
  val registeredMethods = mutable.Map[String, ValueBase => String]()

  def call(method: String, base: ValueBase) = registeredMethods(method)(base)
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
      case _ => "E_BAD_TYPE"
    }
    case _ => "E_NOT_FOUND(" + name + "?)"
  }
}

case class CallItem(method: String, name: String) extends TemplateItem {
  override def build(context: Context) = context(name) match {
    case Some(v) => context.call(method, v)
    case _ => "E_NOT_FOUND(" + name + "?)"
  }
}


case class ForItem(itemName: String, listName: String, internal: TemplateItem) extends TemplateItem {
  override def build(context: Context) = {
    context(listName).orNull match {
      case ValueList(list) =>
        val sb = new StringBuilder()
        for (en <- list) {
          context.push
          context(itemName) = en

          // special handling for pairs TODO
          en match {
            case ValuePair(first, second) =>
              context(itemName + ".first") = first
              context(itemName + ".second") = second
            case _ =>
          }

          sb ++= internal.build(context)
          context.pop
        }
        sb.toString()
      case _ => "Invalid forloop, list=" + listName
    }
  }
}

case class IfItem(name: String, value:String, internal: TemplateItem) extends TemplateItem {
  override def build(context: Context) = {
    context(name).orNull match {
      case ValueString(s) =>
        if (value.equals(s))
          internal.build(context)
        else
          ""
      case _ => ""
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
      case VariableItem(name) => filter(name)
      case ForItem(en, list, internal) => getVariables(internal, exclude + en) ++ filter(list)
      case ListItem(items) => items.map(getVariables(_, exclude)).reduce(_ ++ _)
      case _ => Set.empty
    }
  }
}