package io.scalac

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import monix.eval.Task
import monix.execution.Scheduler
import monix.reactive.Observable
import org.openjdk.jmh.annotations._

import scala.concurrent.Await
import scala.concurrent.duration._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class SimpleBenchmarks {

  object Item

  val items = List.fill(100000)(Item)

  val defaultScheduler = Scheduler.global
  val computationScheduler = Scheduler.computation()
  val ioScheduler = Scheduler.io()

  implicit val system = ActorSystem()
  val mat = ActorMaterializer()

  private def test(s: Scheduler): Unit = {
    val run: Task[Unit] =
      Observable.fromIterable(items)
        .flatMap(Observable(_))
        .map(identity)
        .map(_.hashCode())
        .map(_ * 2)
        .completedL
    Await.result(run.runAsync(s), 1.minute)
  }

  @Benchmark
  def default_scheduler(): Unit = test(defaultScheduler)

  @Benchmark
  def computational_scheduler(): Unit = test(computationScheduler)

  @Benchmark
  def io_scheduler(): Unit = test(ioScheduler)

  @Benchmark
  def akka_default_mat_test(): Unit = {
    val done = Source(items)
      .flatMapConcat(Source.single)
      .map(identity)
      .map(_.hashCode())
      .map(_ * 2)
      .runWith(Sink.ignore)(mat)
    Await.result(done, 1.minute)
  }
}
