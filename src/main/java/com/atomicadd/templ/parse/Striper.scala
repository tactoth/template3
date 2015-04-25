package com.atomica.templ.parse

import scala.collection.mutable
import com.atomicadd.templ._
import java.util.regex.Pattern

// the framework

trait State {
  def handle(c: Char, context: Striper): Unit
  def buildItem: TemplateItem
}

class Striper(input: String) {
  val states = mutable.Stack[State]()

  def strip(baseState: State) = {
    states.push(baseState)
    input.foreach(c => states.top.handle(c, this))
    baseState.buildItem
  }
}

object Striper {
  def strip(input: String, baseState: State = new FreeState) = {
    new Striper(input).strip(baseState)
    baseState.buildItem
  }
}

// commonly used states

class FreeState extends State {
  val templItems = mutable.Buffer[TemplateItem]()
  val sb = StringBuilder.newBuilder

  def handle(c: Char, context: Striper): Unit = {
    sb += c
    if (sb.endsWith("$(")) {
      sb.delete(sb.length - 2, sb.length)
      context.states.push(new CodeState())

      // save current string
      templItems += new StringItem(sb.toString())
      sb.clear()
    }
  }

  def buildItem: TemplateItem = new ListItem(templItems.toList ::: List(StringItem(sb.toString())))

}

class CodeState extends State {
  val sb = StringBuilder.newBuilder
  val codePattern = Pattern.compile("([\\w_]+)(\\s*<-\\s*([\\w_]+))?")

  def handle(c: Char, context: Striper): Unit = {
    sb += c
    if (sb.endsWith(")")) {
      context.states.pop()

      sb.delete(sb.length - 1, sb.length)
      val code = sb.toString()
      println("!!! code=" + code)
      val m = codePattern.matcher(code)
      if (m.find()) {
        val en = m.group(1)
        val list = m.group(3)

        if ("end" equals (en)) {
          // current top is the last state, need to add this state to next
          println("!!! end")
          val lastBuild = context.states.pop().buildItem
          context.states.top.asInstanceOf[FreeState].templItems += lastBuild
        } else if (list == null) {
          // var name
          context.states.top.asInstanceOf[FreeState].templItems += VariableItem(en)
        } else {
          // list, use a new state to handle it
          context.states push new ForBodyState(en, list)
        }
      }
    }
  }

  def buildItem: TemplateItem = null

}

class ForBodyState(en: String, list: String) extends FreeState {
  override def buildItem: TemplateItem = ForItem(en, list, super.buildItem)
}


