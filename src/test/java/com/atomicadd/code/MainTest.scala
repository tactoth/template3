package com.atomicadd.code

import org.junit.{Assert, Test}

/**
 * Created by liuwei on 4/25/15.
 */
class MainTest {

  @Test def testMethods(): Unit = {
    Main.main(Array("methods"))
  }

  @Test def testUssage(): Unit = {
    Main.main(Array("usage"))
  }


}
