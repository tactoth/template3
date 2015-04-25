package com.atomicadd.templ

import com.atomicadd.templ.parse.Striper
import org.junit.{Assert, Test}

/**
 * Created by liuwei on 4/25/15.
 */
class StriperTest {

  @Test def simpleVariable(): Unit = {
    val templateItem = Striper.strip( """hello $(name)""")
    val context = new Context()
    context("name") = ValueString("Wei")

    val result = templateItem.build(context)
    Assert.assertEquals("hello Wei", result)
  }


  @Test def forLoop(): Unit = {
    val templateItem = Striper.strip( """$for(item in list)hello $(item);$end()""")
    val context = new Context()
    context("list") = ValueList(List(ValueString("haha"), ValueString("Guagua")))

    val result = templateItem.build(context)
    Assert.assertEquals("hello haha;hello Guagua;", result)
  }

  @Test def ifState(): Unit = {
    val templateItem = Striper.strip( """$if(condition)hello$end()world""")

    {
      val context = new Context()
      context("condition") = ValueString("true")

      val result = templateItem.build(context)
      Assert.assertEquals("helloworld", result)
    }

    {
      val context = new Context()
      context("condition") = ValueString("false")

      val result = templateItem.build(context)
      Assert.assertEquals("world", result)
    }
  }

  @Test def testCall(): Unit = {
    val templateItem = Striper.strip( """$(name) and $call:reverse(name)""")

    val context = new Context()
    context.registeredMethods("reverse") = (vs: ValueBase) => vs.asInstanceOf[ValueString].str.reverse

    context("name") = ValueString("Wei")

    val result = templateItem.build(context)
    Assert.assertEquals("Wei and ieW", result)

  }


}