package com.atomicadd.code

import java.util.regex.Pattern

import scala.collection.mutable

trait ValueBase {
}

case class ValueString(str: String) extends ValueBase {}

case class ValuePair(first: ValueBase, second: ValueBase) extends ValueBase {}

case class ValueList(list: Seq[ValueBase]) extends ValueBase {}

// dynamically resolved value
case class ValueDynamic(method: String, parameterName: String) extends ValueBase {
  var resolved: ValueBase = null
}


class Context {

  // managing variables
  private val varStack = mutable.Buffer[mutable.Map[String, ValueBase]]()

  private def currentVars = varStack.last

  def push = varStack += mutable.Map[String, ValueBase]()

  def pop = varStack.remove(varStack.size - 1)

  // get value, even if it's dynamic, we still return directly
  private def get(name: String) = varStack.reverse.collectFirst {
    // loves partial function
    case m if m.contains(name) => m(name)
  }

  def apply(name: String): Option[ValueBase] = {
    val got = get(name)
    got match {
      case Some(dynamic: ValueDynamic) =>
        if (dynamic.resolved != null)
          Some(dynamic.resolved)
        else
          Some(call(dynamic.method, apply {
            dynamic.parameterName
          }.get))
      case _ => got
    }
  }

  def update(name: String, value: ValueBase) = currentVars(name) = value

  def update(name: String, repr: String): Unit = {
    val matcherDynamic = Context.PATTERN_DYNAMIC.matcher(repr)
    val matcherCompute = Context.PATTERN_COMPUTE.matcher(repr)

    if (matcherDynamic.find()) {
      update(name, ValueDynamic(matcherDynamic.group(1), matcherDynamic.group(2)))
    } else if (matcherCompute.find()) {
      update(name, call(matcherCompute.group(1), ValueString(matcherCompute.group(2))))
    } else {
      update(name, ValueString(repr))
    }
  }

  // push default map
  push

  // plugins for users
  val registeredMethods = mutable.Map[String, ValueBase => ValueBase]()

  def call(method: String, base: ValueBase) = registeredMethods(method)(base)

  // register built-in methods
  private def makeList(s: String) = {
    val valueList = s.split(",").map(ValueString).toList
    ValueList(valueList)
  }

  private def makeDictionary(s: String) = {
    val values = s.split(",").map(
      item =>
        item.split(":") match {
          case Array(key, value) => ValuePair(ValueString(key), ValueString(value))
          case _ => throw new RuntimeException("Invalid input for pair entry: " + item)
        }
    ).toList
    ValueList(values)
  }

  registeredMethods("list") = {
    v =>
      makeList(v.asInstanceOf[ValueString].str)
  }

  registeredMethods("map") = {
    v =>
      makeDictionary(v.asInstanceOf[ValueString].str)
  }

}

object Context {
  val PATTERN_DYNAMIC = Pattern compile """\$:(\w+)\((\w+)\)"""

  // compute, doesn't depend on anything
  val PATTERN_COMPUTE = Pattern compile """\$:(\w+)<(.*)>"""
}
