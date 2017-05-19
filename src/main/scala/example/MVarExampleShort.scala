package example

import monix.eval.{MVar, Task}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object MVarExampleShort extends App {

  import monix.execution.Scheduler.Implicits.global

  val N = 100000
  val mvar = MVar.empty[Int]

  def produce(n: Int): Task[Unit] =
    if (n < N)
      mvar.put(n).flatMap(_ => produce(n + 1))
    else
      Task.now(())

  def consume(sum: Long, c: Int): Task[Long] =
    if (c < N) {
      mvar.take
        .timeout(100.millisecond)
        .flatMap(v => consume(v + sum, c + 1))
    } else {
      Task.now(sum)
    }

  val consumer = consume(0, 0).runAsync
  val producer = produce(0).runOnComplete {
    case Success(_) => println("Producer finished.")
    case Failure(ex) => println(s"Producer failed: $ex")
  }
  consumer.onComplete(r => println(s"Consumer result: $r"))
  Await.result(consumer, 5.seconds)
}
