package chatbot

import java.io.File
import scalaj.http.{Http, HttpOptions}
import scala.collection.immutable.SortedMap
import scala.concurrent.Future
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object ChatBot extends App {

  case class Tests(fileName: String, customerInput: String, botResponse: String)

  val TestsPath = "./src/main/scala/chatbot/spec"
  val ShortCode = "12345"
  val APIAddress = s"API"
  var Scenery = List.empty[ChatBot.Tests]
  var FileNames = Map.empty[String, String]

  testBot()

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def getFileNames: List[String] = {
    val folder = new File(TestsPath)
    var postName = List.empty[String]
    if (folder.exists && folder.isDirectory) {
      val names = folder.listFiles.toList.map(a => a.toString)
      postName = postName ::: names
    }
    postName
  }


  def testBot():Unit = {
    val mapScenario = getTestsInMap
    val getFileNames = mapScenario.keys.toList

    println(s"TESTS DETECTED: ${getFileNames.length}")
    val response = for {
      i <- getFileNames.indices  // getfileNames.length
      resultF = runTests(mapScenario, getFileNames, i)
//      j <-getfileNames.length/2 until getfileNames.length
//      result2F = runTests(mapScenery, getfileNames, j)
    } yield resultF

//    Thread.sleep(10000)
    val finalErrorResult = response.toList.map(_._1).sum
    val finalSuccessResult = response.toList.map(_._2).sum
    println("===============")
    println("ERROR: " + finalErrorResult)
    println("SUCCESS: " + finalSuccessResult)
    println("===============")



  }

  def runTests(mapScenario: Map[String, List[ChatBot.Tests]], fileName: List[String], i: Int): (Int, Int) =  {
//  def runTests(mapScenery: Map[String, List[ChatBot.Tests]], fileName: List[String], i: Int): Future[(Int, Int)] = Future {  // for parallel requests
    var done = 0
    var error = 0
    var currentTestFile = ""
    var failedTestKey = ""
    val getList = mapScenario(fileName(i))
    println(getList.head.fileName + "_convo.txt is running...")
    getList.foreach { key =>
      if (failedTestKey != key.customerInput) {
        val response = callApiAndSendMsg(key.customerInput, key.fileName)
        if (response != key.botResponse) {
          error += 1
          println(s"ERROR in file: ${key.fileName}:\n\n'$response' doesn't match: '${key.botResponse}'\n")
          failedTestKey = key.customerInput
        } else if (currentTestFile != key.fileName) {
          done += 1
        }
      }
      if (currentTestFile != key.fileName) {
        currentTestFile = key.fileName
      }
    }
    (error, done)
  }

  def getTestsInMap: Map[String, List[ChatBot.Tests]] = {
    var i = 1
    getFileNames.foreach { a =>
      val list = readFromFile(a).toList.filterNot(p => p == "" || p == "#bot" || p == "#me")
      i = 1
      while (i < list.length - 1) {
        Scenery = Scenery :+ Tests(list.head, list(i), list(i + 1))
        i += 2
      }
    }
    SortedMap(Scenery.groupBy(_.fileName).toSeq:_*)
  }

  def callApiAndSendMsg(key: String, fileName: String): String = {
    val randomPhone = if (FileNames.nonEmpty && FileNames.contains(fileName)) {
      FileNames(fileName)
    } else {
      val newRandomPhone = getRandomDigits(10)
      FileNames = FileNames + (fileName -> newRandomPhone)
      newRandomPhone
    }
    val data = s"""{"from":"$randomPhone","to":"$ShortCode","body":"$key"}"""
    Http(s"$APIAddress").postData(data)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString.body.replaceAll("\\s+", " ").split('\n').map(_.trim.filter(_ >= ' ')).mkString(" ")
//    val map = Map.empty[String, String]  //to test need to fill Map
//    map(key)
  }

  def readFromFile(file: String): Iterator[String] = {
    for (line <- Source.fromFile(file).getLines) yield line
  }

  def getRandomDigits(length: Int) = {
    Seq.fill(length)(Random.nextInt(9)).mkString("")
  }

}
