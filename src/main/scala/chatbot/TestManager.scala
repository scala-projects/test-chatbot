package chatbot

import akka.actor._
import akka.pattern.pipe
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

case class CompleteTests(mapScenario: Map[String, List[ChatBot.Tests]], fileNames: List[String], i: Int)

case class FillMapWithFileNameAndRandomPhone(fileNames: List[String])

object TestManager {
  def props = Props(new TestManager)
}

class TestManager extends Actor {

  val ShortCode = "12345"
  val APIAddress = s"API_URL"
  var FileNames = Map.empty[String, String]

  override def receive: Receive = {
    case CompleteTests(mapScenario, fileName, i) =>
      completeTests(mapScenario, fileName, i).pipeTo(sender())

    case FillMapWithFileNameAndRandomPhone(fileNames) =>
      fillFileNamesWithPhone(fileNames).pipeTo(sender())
  }


  private def completeTests(mapScenario: Map[String, List[ChatBot.Tests]], fileName: List[String], i: Int): Future[(Int, Int)] = Future {
    var done = 0
    var error = 0
    var currentTestFile = ""
    var failedTestKey = ""
    val getList = mapScenario(fileName(i))
    println(getList.head.fileName + " is running...")
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

  private def callApiAndSendMsg(key: String, fileName: String): String = {
    val randomPhone = if (FileNames.nonEmpty && FileNames.contains(fileName)) {
      FileNames(fileName)
    } else {
      sys.error(s"no fileName: $fileName in map")
    }
//    val data = s"""{"from":"$randomPhone","to":"$ShortCode","body":"$key"}"""
//    Http(s"$APIAddress").postData(data)
//      .header("Content-Type", "application/json")
//      .header("Charset", "UTF-8")
//      .option(HttpOptions.readTimeout(10000)).asString.body.replaceAll("\\s+", " ").split('\n').map(_.trim.filter(_ >= ' ')).mkString(" ")
    val map = Map("Hi" -> "Hello! How I can help you?", "What can you do?" -> "I can help you to improve your knowledge in Scala Programming", "Wow! Great!" -> "Let's start", "Hi2" -> "Hello! How I can help you?2", "What can you do?2" -> "I can help you to improve your knowledge in Scala Programming2", "Wow! Great!2" -> "Let's start2")
    map(key)
  }

  private def fillFileNamesWithPhone(fileNames: List[String]): Future[Unit] = Future {
    fileNames.foreach { fileName =>
      val newRandomPhone = getRandomDigits(10)
      FileNames = FileNames + (fileName -> newRandomPhone)
    }
  }

  private def getRandomDigits(length: Int) = {
    Seq.fill(length)(Random.nextInt(9)).mkString("")
  }
}