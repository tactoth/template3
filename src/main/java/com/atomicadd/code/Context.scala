package com.atomicadd.code

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
