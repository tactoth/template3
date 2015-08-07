package com.atomicadd.code

import java.io.{FileFilter, File}
import java.nio.file.{Files, Paths}
import java.util

import com.atomicadd.code.parse._
import com.atomicadd.code.plugins.ViewFieldsGenerator
import com.atomicadd.code.utils.Utils
import com.beust.jcommander.{Parameters, DynamicParameter, JCommander, Parameter}

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
 * Main
 * Created by liuwei on 4/25/15.
 */
object Main {

  /**
   * Global options
   */
  object options {
    @Parameter(names = Array("-templates", "-TD"), description = "Template file directory", required = false)
    var templatesDir: File = new File("templates")

    def getTemplates(query: String) = {
      templatesDir.listFiles(
        new FileFilter {

          def fuzzyMatch(query: String, s: String): Boolean = {
            if (query.isEmpty)
              return true

            (query.length <= s.length) && {
              if (query.head == s.head) fuzzyMatch(query.tail, s.tail)
              else fuzzyMatch(query, s.tail)
            }
          }

          override def accept(file: File): Boolean = {
            !file.isDirectory && fuzzyMatch(query.toLowerCase, file.getName.toLowerCase)
          }
        }
      )
    }

    def getTemplateFile(query: String) = {
      val files = getTemplates(query)
      if (files == null || files.isEmpty) {
        println(s"no template found for $query")
        println(s"templates dir = $templatesDir")
        throw new RuntimeException
      } else if (files.length > 1) {
        println(s"${files.length} files found:")
        files.foreach(println(_))
        throw new RuntimeException
      } else {
        println(s"template match $query => ${files.head}")
        files.head
      }
    }
  }

  def main(args: Array[String]) {

    val commander = new JCommander()
    commander.addObject(options)

    val actions = mutable.Map[String, Runnable]()

    def addCommand(name: String, action: Runnable): Unit = {
      commander.addCommand(name, action)
      actions(name) = action
    }

    // add commands
    addCommand("gen", new BuildOptions)
    addCommand("params", new PrintParamsOptions)
    addCommand("templates", new PrintTemplates)
    addCommand("methods", new PrintMethods)


    // parse
    commander.parse(args: _*)
    val parsedCommand = commander.getParsedCommand
    if (parsedCommand == null) {
      commander.usage()
    } else {
      actions(parsedCommand).run()
    }
  }


  @Parameters(commandDescription = "Print parameter list of the template")
  class PrintParamsOptions extends Runnable {
    @Parameter(description = "Template files")
    val templates = new util.ArrayList[String]()

    override def run(): Unit = {
      val varNames = for (templateQuery <- this.templates) yield {
        val content = Utils.readAll(options.getTemplateFile(templateQuery))
        val template = Template(content)
        template.getVariables
      }

      varNames.reduce(_ ++ _).foreach(println(_))
    }
  }

  @Parameters(commandDescription = "Print templates")
  class PrintTemplates extends Runnable {
    override def run(): Unit = {
      options.templatesDir.listFiles().foreach(println(_))
    }
  }

  @Parameters(commandDescription = "Print methods")
  class PrintMethods extends BaseOptions {
    override def run(): Unit = {
      createContext.registeredMethods.keys.foreach(println(_))
    }
  }

  abstract class BaseOptions extends Runnable {
    @DynamicParameter(
      names = Array("-D"),
      description = "Your template input parameters, can be plain string, or $:method(variableName), or $:method<plain string>,"
        + " list syntax:item,item,item; map syntax: a:1,b:2,c:3"
        + " to see a list of methods use \"methods\"")
    val values = new util.HashMap[String, String]()

    def parseContext() = {
      val context: Context = createContext

      for (en <- this.values) {
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

  }

  @Parameters(commandDescription = "Generate content based on template and template input")
  class BuildOptions extends BaseOptions {
    @Parameter(names = Array("-template", "-T"), description = "Template file", required = true)
    var template: String = ""

    @Parameter(names = Array("-out", "-O"), description = "Out put file")
    var out: String = "std"

    override def run(): Unit = {
      val context = parseContext()

      val lines = Utils.readAll(options.getTemplateFile(this.template))

      val template = Striper.strip(lines)
      val str = template.build(context)

      if ("std".equals(out)) {
        println(str)
      } else {
        Files.write(Paths.get(out), str.getBytes("utf-8"))
      }

    }
  }

}
