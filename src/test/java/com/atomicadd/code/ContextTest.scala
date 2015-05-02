package com.atomicadd.code

import org.junit.{Assert, Test}

/**
 * Created by liuwei on 4/25/15.
 */
class ContextTest {

  @Test def testMakeList(): Unit = {
    val context = new Context()
    context("hello") = "$:list<1,2,3>"
    val value = context("hello")
    value match {
      case Some(ValueList(Seq(ValueString("1"), ValueString("2"), ValueString("3")))) =>
      // pass
      case _ => Assert.fail("Parse fail, result=" + value)
    }
  }


  @Test def testMakeMap(): Unit = {
    val context = new Context()
    context("hello") = "$:map<1:a,2:b,3:c>"
    val value = context("hello")
    value match {
      case Some(ValueList(Seq(ValuePair(ValueString("1"), ValueString("a")),
      ValuePair(ValueString("2"), ValueString("b")),
      ValuePair(ValueString("3"), ValueString("c"))))) =>
      // pass
      case _ => Assert.fail("Parse fail, result=" + value)
    }
  }


  @Test def testCallMethod(): Unit = {
    val context = new Context()
    context("str") = "c:3,b:2,a:1"
    context("reversedStr") = "$:reverse(str)"
    context("hello") = "$:map(reversedStr)"

    context.registeredMethods("reverse") = (vs: ValueBase) =>  ValueString(vs.asInstanceOf[ValueString].str.reverse)

    val value = context("hello")
    value match {
      case Some(ValueList(Seq(ValuePair(ValueString("1"), ValueString("a")),
      ValuePair(ValueString("2"), ValueString("b")),
      ValuePair(ValueString("3"), ValueString("c"))))) =>
      // pass
      case _ => Assert.fail("Parse fail, result=" + value)
    }
  }


}
