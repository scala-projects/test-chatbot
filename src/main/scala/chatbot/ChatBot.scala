package chatbot

import java.io.File
import java.util.Date

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.immutable.SortedMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.Source

object ChatBot extends App {

  case class Tests(fileName: String, customerInput: String, botResponse: String)

  val TestsPath = "./src/main/scala/chatbot/spec"
  var Scenarios = List.empty[ChatBot.Tests]
  var FileNames = Map.empty[String, String]

  implicit val defaultTimeout: Timeout = Timeout(60.seconds)
  val system = ActorSystem()
  val completeTestManager = system.actorOf(TestManager.props)

  startTests()

  def startTests() = {
    println("Started: " + new Date)
    for {
      _ <- completeTestManager ? FillMapWithFileNameAndRandomPhone(getTestsInMap.keys.toList)
      _ <- testBot()
    } yield {
      println("Finished: " + new Date)
      ()
    }
  }

  def getListOfFiles(dir: String): List[File] = {
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


  def testBot() = {
    val mapScenario = getTestsInMap
    val fileNames = mapScenario.keys.toList

    println(s"TESTS DETECTED: ${fileNames.length}")
    val responseF1 = Future.sequence((0 until fileNames.length / 2).map { i =>
      (completeTestManager ? CompleteTests(mapScenario, fileNames, i)).mapTo[(Int, Int)]
    })
    val responseF2 = Future.sequence((fileNames.length / 2 until fileNames.size).map { j =>
      (completeTestManager ? CompleteTests(mapScenario, fileNames, j)).mapTo[(Int, Int)]
    })

    (for {
      r1 <- responseF1
      r2 <- responseF2
    } yield {
      val errorsF = r1.toList.map(_._1) ::: r2.toList.map(_._1)
      val successF = r1.toList.map(_._2) ::: r2.toList.map(_._2)
      (errorsF.sum, successF.sum)
    }).map { results =>
      println("===============")
      println("ERROR: " + results._1)
      println("SUCCESS: " + results._2)
      println("===============")
    }.recover {
      case error =>
        sys.error(error.toString)
    }

  }

  def getTestsInMap: Map[String, List[ChatBot.Tests]] = {
    var i = 1
    getFileNames.foreach { a =>
      val list = readFromFile(a).toList.filterNot(p => p == "" || p == "#bot" || p == "#me")
      i = 1
      while (i < list.length - 1) {
        Scenarios = Scenarios :+ Tests(list.head, list(i), list(i + 1))
        i += 2
      }
    }
    SortedMap(Scenarios.groupBy(_.fileName).toSeq: _*)
  }

  def readFromFile(file: String): Iterator[String] = {
    for (line <- Source.fromFile(file).getLines) yield line
  }

}
