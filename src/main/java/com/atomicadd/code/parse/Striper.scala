package com.atomicadd.code.parse

import java.util.regex.Pattern

import scala.collection.mutable

// the framework

trait State {
  def handle(c: Char, striper: Striper): Unit

  def buildItem: TemplateItem
}

class Striper(input: String) {

  val states = mutable.Stack[State]()

  def topState = states.top
  def topAsFreeState = topState.asInstanceOf[FreeState]

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

object FreeState {
  val CODE_START_PATTERN = Pattern.compile("""\$([\w:]*)\(\z""")
}

class FreeState extends State {
  val templItems = mutable.Buffer[TemplateItem]()
  val sb = StringBuilder.newBuilder


  def handle(c: Char, striper: Striper): Unit = {
    sb += c
    val matcher = FreeState.CODE_START_PATTERN.matcher(sb)
    if (matcher.find()) {
      val label = matcher.group(1)
      val sectionBegin = matcher.group()

      sb.delete(sb.length - sectionBegin.length, sb.length)
      striper.states.push(new CodeState(label))
      // save current string
      templItems += new StringItem(sb.toString())
      sb.clear()
    }
  }

  def buildItem: TemplateItem = new ListItem(templItems.toList ::: List(StringItem(sb.toString())))
}

object CodeState {
  val CALL_PATTERN = Pattern.compile(""":(\w+)""")
  val FOR_PATTERN = Pattern.compile( """(\w+)\s+in\s+(\w+)""")
  val IF_PATTERN = Pattern.compile("""(\w+)\s*==\s*\"(\w+)\"""")
}

class CodeState(label: String) extends State {
  val sb = StringBuilder.newBuilder

  def handle(c: Char, striper: Striper): Unit = {
    sb += c
    if (sb.endsWith(")")) {
      striper.states.pop()

      sb.delete(sb.length - 1, sb.length)
      val code = sb.toString()

      // handling labels
      label match {
        case "" =>
          striper.topAsFreeState.templItems += VariableItem(code)
        case "if" =>
          val m = CodeState.IF_PATTERN.matcher(code)
          if (m.matches()) {
            val name = m.group(1)
            val expected = m.group(2)
            striper.states push new WrappedState(item => IfItem(name, expected, item))
          } else {
            println("Invalid if body: " + code + ", should be name==\"value\"")
          }
        case "for" =>
          val m = CodeState.FOR_PATTERN.matcher(code)
          if (m.matches()) {
            val itemName = m.group(1)
            val listName = m.group(2)
            striper.states push new WrappedState(item => ForItem(itemName, listName, item))
          } else {
            println("Invalid for body: " + code + ", should be item in list")
          }
        case "end" =>
          val lastBuild = striper.states.pop().buildItem
          striper.topAsFreeState.templItems += lastBuild
        case _ =>
          val matcherCall = CodeState.CALL_PATTERN.matcher(label)
          if (matcherCall.matches()) {
            val method = matcherCall.group(1)
            striper.topAsFreeState.templItems += CallItem(method, code)
          } else {
            println("Unrecognized Code block, label=" + label + ", code=" + code)
          }
      }
    }
  }

  def buildItem: TemplateItem = null

}

class WrappedState(wrappedItemBuilder: TemplateItem => TemplateItem) extends FreeState {
  override def buildItem: TemplateItem = wrappedItemBuilder(super.buildItem)
}


