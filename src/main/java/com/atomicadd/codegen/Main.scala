package com.atomicadd.codegen

import java.nio.file.{Files, Paths}
import java.util

import com.atomicadd.templ.parse.Striper
import com.atomicadd.templ._
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

        val source = scala.io.Source.fromFile(buildOptions.template)
        val lines = try source.getLines().mkString("\n") finally source.close()

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

    def makeList(s: String) = {
      val valueList = s.split(",").map(ValueString(_)).toList
      ValueList(valueList)
    }

    def makeDictionary(s: String) = {
      val values = s.split(",").map(
        item =>
          item.split(":") match {
            case Array(key, value) => ValuePair(ValueString(key), ValueString(value))
            case _ => throw new RuntimeException("Invalid input for pair entry: " + item)
          }
      ).toList
      ValueList(values)
    }

    def transform(map: util.HashMap[String, String], f: String => ValueBase) = {
      for (en <- map) yield en match {
        case (key, value) => (key, f(value))
      }
    }

    val opts = transform(options.values, ValueString) ::
      transform(options.lists, makeList) ::
      transform(options.maps, makeDictionary) :: Nil


    for (values <- opts; entry <- values) {
      context(entry._1) = entry._2
    }

    context
  }

  class BaseOptions {
    @DynamicParameter(names = Array("-D"), description = "Your template input parameters")
    val values = new util.HashMap[String, String]()

    @DynamicParameter(names = Array("-L"), description = "List input parameters, format: item1,item2,item3")
    val lists = new util.HashMap[String, String]()

    @DynamicParameter(names = Array("-M"), description = "Map input parameters, format: key1:value1,key2:value2")
    val maps = new util.HashMap[String, String]()
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
