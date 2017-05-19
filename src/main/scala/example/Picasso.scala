package example

import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.TimeoutException
import scala.concurrent.duration._
import scala.util.Random

object Picasso extends App {

  implicit val computationScheduler = Scheduler.computation()
  val ioScheduler = Scheduler.io()

  type Picture = String
  val maybePicassoImage =
    """
      |..\\....
      |...\\...
      |....\\..
      |....//..
      |...//...
      |..//....
    """

  sealed trait AnalysisResult

  case object NotPicasso extends AnalysisResult

  case object Picasso extends AnalysisResult

  case object HardToSay extends AnalysisResult

  //Monix gives plethora of combinators but there is a place for one more 
  def retryWithDelay[A](t: Task[A], delay: FiniteDuration, restarts: Int) =
    t.onErrorFallbackTo(t.delayExecution(delay).onErrorRestart(restarts))

  def analyseLocally(pic: Picture): Task[AnalysisResult] =
    Task.now(HardToSay) // Mock, eagerly wraps HardToSay in Task, like Future.successful
      .executeOn(computationScheduler)

  def analyseRemotely(pic: Picture): Task[AnalysisResult] =
    Task.eval(Picasso) // Mock...
      .delayResult(3.seconds) // ... with artificial delay
      .executeOn(ioScheduler)

  def storeResult(pic: Picture, result: AnalysisResult): Task[Unit] =
    Task.eval(()) // Mock...
      .delayResult(Random.nextInt(5).seconds) // ... with artificial delay
      .timeout(3.seconds) // Task will fail if does not complete in 3 seconds
      .onErrorRecover {
      case ex: TimeoutException =>
        println(s"Logging severe error to bring human attention, $ex")
    }

  def analyseAndStoreResult(pic: Picture) =
    analyseLocally(pic).flatMap {
      case HardToSay =>
        retryWithDelay(analyseRemotely(pic), 3.seconds, 5)
      case resolvedResult: AnalysisResult =>
        Task.now(resolvedResult)
    }.flatMap(storeResult(pic, _).executeOn(ioScheduler))

  analyseAndStoreResult(maybePicassoImage).runAsync //CancellableFuture[Unit]

}
