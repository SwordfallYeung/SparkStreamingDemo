package com.swordfall.streamingkafka

import java.io._
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util
import java.util.{Date, TimeZone}

import com.swordfall.streamingkafka.SparkStreamingKafka.MyRecord

import scala.util.Random

object SparkStreamingKafkaTest {

  def createContentString(): String ={
    val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"))
    val dateStr = sdf.format(new Date)

    val pcIdArray = Array("DEIBAH","GLLIEG","HIJMEC","HMGBDE","HIJFLA","JCEBBC","KJLAKG","FHEIKI","IGIDLB","IIIJCD")
    val random = new Random
    val pcIdIndex = random.nextInt(10)

    val siteId = random.nextInt(20)
    val content = dateStr + s"|~|200|~|/test?pcid=" + pcIdArray(pcIdIndex) + s"&siteid=" + siteId
    content
  }

  def createLogFile(): Unit ={
    val sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val file = new File(sdf.format(new Date()) + "_nginx.log")
    val writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)
    var content = ""
    for (i <- 0 to 1000){
        content = createContentString()
      //println(content)
      writer.write(content + "\n")
    }
    writer.close()
  }

  def parseLog(line: String): Option[MyRecord] = {
    val ary: Array[String] = line.split("\\|~\\|", -1)
    try{
      val hour = ary(0).substring(0, 13).replace("T", "-")
      val uri = ary(2).split("[=|&]", -1)
      val user_id = uri(1)
      val site_id = uri(3)
      return scala.Some(MyRecord(hour, user_id, site_id))
    }catch{
      case ex: Exception => println(ex.getMessage)
    }
    return None
  }

  def parseLogTest(): Unit ={
    val string = "2019-09-21T17:14:11.117|~|200|~|/test?pcid=KJLAKG&siteid=11"
    val result = parseLog(string)
    println(result)
  }

  def main(args: Array[String]): Unit = {
    var mode = true
    while (mode){
      try{
        println("please input test mode:")
        println("1. getDateString")
        println("2. createLogFile")
        println("3. parseLogTest")
        val in = new InputStreamReader(System.in)
        val reader = new BufferedReader(in)
        val inputStr = reader.readLine()
        inputStr match {
          case "1" => createContentString()
          case "2" => createLogFile()
          case "3" => parseLogTest()
          case _ => mode = false
        }

      }catch {
        case ex: Exception => {
          println(ex)
          mode = false
        }
      }
    }

  }
}
