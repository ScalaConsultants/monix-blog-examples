package example

import monix.eval.Coeval

import scala.io.StdIn
import scala.util.control.NonFatal

object CoevalExample extends App {

  //Coeval[A] can be seen also as `Function0[A]`
  val prompt: Coeval[Unit] = Coeval.eval(println("Please enter a number"))
  val lastPrompt: Coeval[Unit] = Coeval.eval(println(s"Please enter a number, otherwise 42 will be used"))
  val readLine: Coeval[String] = Coeval.eval(StdIn.readLine())

  def promptForNumber(prompt: Coeval[Unit]): Coeval[Int] =
    for {
      _ <- prompt
      s <- readLine
    } yield s.toInt

  val num = promptForNumber(prompt)
    .onErrorRestart(2)
    .onErrorFallbackTo(promptForNumber(lastPrompt))
    .onErrorRecover {
      case NonFatal(_) => 42
    }.memoize

  println(s"${num.value}")
  println(s"${num.value}")
}
