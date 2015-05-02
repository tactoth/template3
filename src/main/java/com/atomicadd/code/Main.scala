package com.atomicadd.code

import java.io.File
import java.nio.file.{Files, Paths}
import java.util

import com.atomicadd.code.parse._
import com.atomicadd.code.plugins.ViewFieldsGenerator
import com.atomicadd.code.utils.Utils
import com.beust.jcommander.{Parameters, DynamicParameter, JCommander, Parameter}

import scala.collection.JavaConversions._

/**
 * Created by liuwei on 4/25/15.
 */
object Main {
  val CMD_GEN: String = "gen"

  val CMD_METHODS: String = "methods"

  val CMD_BATCH: String = "batch"

  val CMD_PRINT_PARAMS: String = "printParams"

  def main(args: Array[String]) {

    val commander = new JCommander()
    val batchOptions = new BatchOptions()
    val buildOptions = new BuildOptions()
    val printParamsOptions = new PrintParamsOptions

    commander.addCommand(CMD_BATCH, batchOptions)
    commander.addCommand(CMD_GEN, buildOptions)
    commander.addCommand(CMD_METHODS, new Object)
    commander.addCommand(CMD_PRINT_PARAMS, printParamsOptions)

    try {
      commander.parse(args: _*)
    } catch {
      case e: Throwable => println(e.getMessage)
        val stringBuilder = new java.lang.StringBuilder()
        commander.usage(stringBuilder)
        println(stringBuilder)
        return
    }

    commander.getParsedCommand match {
      case CMD_GEN =>
        // parse options
        val context = parseContext(buildOptions)

        val lines = Utils.readAll(new File(buildOptions.template))

        val template = Striper.strip(lines)
        val str = template.build(context)

        if ("std".equals(buildOptions.out)) {
          println(str)
        } else {
          Files.write(Paths.get(buildOptions.out), str.getBytes("utf-8"))
        }

      case CMD_METHODS =>
        // print all method names
        createContext.registeredMethods.keys.foreach(println(_))
      case CMD_PRINT_PARAMS =>
        val varNames = for (file <- printParamsOptions.templateFiles) yield {
          val content = Utils.readAll(new File(file))
          val template = Template(content)
          template.getVariables
        }

        varNames.reduce(_ ++ _).foreach(println(_))
      case CMD_BATCH =>
      // TODO, implement this
      case _ =>
    }
  }

  def parseContext(options: BaseOptions) = {
    var context: Context = createContext

    for (en <- options.values) {
      en match {
        case (key, value) => context(key) = value
      }
    }

    context
  }

  def createContext = {
    val context = new Context
    context.registeredMethods("android_views") = {
      vs =>
        ViewFieldsGenerator.viewsAsValue(new File(vs.asInstanceOf[ValueString].str.replace("~", System.getProperty("user.home"))))
    }
    context
  }

  @Parameters(commandDescription = "Print parameter list of the template")
  class PrintParamsOptions {
    @Parameter
    val templateFiles = new util.ArrayList[String]()
  }

  class BaseOptions {
    @DynamicParameter(
      names = Array("-D"),
      description = "Your template input parameters, can be plain string, or $:method(variableName), or $:method<plain string>,"
        + " to see a list of methods use \"methods\"")
    val values = new util.HashMap[String, String]()
  }

  class BatchOptions extends BaseOptions {
    val listFile = "list.xml"
  }

  class BuildOptions extends BaseOptions {
    @Parameter(names = Array("-template", "-T"), description = "Template file", required = true)
    var template: String = ""

    @Parameter(names = Array("-out", "-O"), description = "Out put file")
    var out: String = "std"
  }

}
