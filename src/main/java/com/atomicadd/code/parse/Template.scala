package com.atomicadd.code.parse

import com.atomicadd.code.{Context, ValueList, ValuePair, ValueString}


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

case class IfItem(name: String, value: String, internal: TemplateItem) extends TemplateItem {
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



