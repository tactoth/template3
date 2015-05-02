package com.atomicadd.code

import java.io.File
import java.nio.file.{Files, Paths}
import java.util

import com.atomicadd.code.parse._
import com.atomicadd.code.utils.Utils
import com.beust.jcommander.{DynamicParameter, JCommander, Parameter}

import scala.collection.JavaConversions._

/**
 * Created by liuwei on 4/25/15.
 */
object Main {
  val CMD_GEN: String = "gen"

  val CMD_BATCH: String = "batch"

  def main(args: Array[String]) {
    val commander = new JCommander()
    val batchOptions = new BatchOptions()
    val buildOptions = new BuildOptions()

    commander.addCommand(CMD_BATCH, batchOptions)
    commander.addCommand(CMD_GEN, buildOptions)

    try {
      commander.parse(args: _*)
    } catch {
      case e: Throwable => println(e.getMessage)
        val stringBuilder = new java.lang.StringBuilder()
        commander.usage(stringBuilder)
        println(stringBuilder)
    }

    commander.getParsedCommand match {
      case CMD_GEN =>
        // parse options
        val context = parseContext(buildOptions)

        val lines = Utils.readAll(new File(buildOptions.template))

        val template = Striper.strip(lines)
        val str = template.build(context)

        Files.write(Paths.get(buildOptions.out), str.getBytes("utf-8"))
      case CMD_BATCH =>
      // TODO, implement this
      case _ =>
    }
  }

  def parseContext(options: BaseOptions) = {
    var context: Context = new Context

    for (en <- options.values) {
      en match {
        case (key, value) => context(key) = value
      }
    }

    context
  }

  class BaseOptions {
    @DynamicParameter(names = Array("-D"), description = "Your template input parameters, can be plain string, or $:method(variableName), or $:method<plain string>")
    val values = new util.HashMap[String, String]()
  }

  class BatchOptions extends BaseOptions {
    val listFile = "list.xml"
  }

  class BuildOptions extends BaseOptions {
    @Parameter(names = Array("-template", "-T"), description = "Template file", required = true)
    var template: String = ""

    @Parameter(names = Array("-out", "-O"), description = "Out put file", required = true)
    var out: String = ""
  }

}
