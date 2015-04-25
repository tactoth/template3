package com.atomicadd.code

import java.io.File

object JoggingUtils {
  def main(args: Array[String]): Unit = {
    ListModelGenerator.generateListModel(
      "com.toptal.joggingtracker",
      "model",
      "pages",
      "WeeklyReport",
      "item_weekly_report",
      List("year", "week", "totalDuration", "totalDistance"),
      new File("/Users/liuwei/Code/toptal/JoggingTrackerAndroid/app/src/main"))

    //    ListModelGenerator.generateListModel(
    //      "com.toptal.joggingtracker",
    //      "model",
    //      "pages",
    //      "TestJoggingItem",
    //      "item_test_jogging_item",
    //      List("time", "name", "avatar"),
    //      new File("/Users/liuwei/Code/toptal/JoggingTrackerAndroid/app/src/main"))
  }
}