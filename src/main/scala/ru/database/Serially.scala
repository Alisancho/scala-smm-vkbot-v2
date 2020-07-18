package ru.database

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

trait Serially {
  def serially[A, B, M[X] <: TraversableOnce[X]](in: M[A])(fn: A => Future[B])(implicit cbf: CanBuildFrom[M[A], B, M[B]],
                                                                               executor: ExecutionContext): Future[M[B]] =
    in.foldLeft(Future.successful(cbf(in))) { (acc, elem) =>
        for (builder <- acc; b <- fn(elem)) yield builder += b
      }
      .map(_.result())
}
