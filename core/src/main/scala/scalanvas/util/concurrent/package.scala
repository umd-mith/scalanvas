package edu.umd.mith.scalanvas.util

import scalaz.{ Nondeterminism, Validation }
import scalaz.concurrent.{ Future, Task }

package object concurrent {
  implicit class OptionToTask[A](val option: Option[A]) extends AnyVal {
    def toTask(failure: Throwable): Task[A] = option.fold[Task[A]](
      Task.fail(failure)
    )(Task.now)
  }

  def validationTask[A](v: => Validation[Throwable, A]) =
    new Task(Future.delay(v.disjunction))
}

