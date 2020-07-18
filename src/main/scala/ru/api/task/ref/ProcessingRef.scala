package ru.api.task.ref

import akka.actor.ActorRef
import akka.stream.SharedKillSwitch
import cats.effect.IO
import cats.effect.concurrent.Ref

sealed trait MainRef

sealed trait MyActorRef extends MainRef {
  def getRef: IO[Option[ActorRef]]
  def setRef(tag: ActorRef): IO[Unit]
  def setNone: IO[Unit]
}

sealed trait MyStrrmRef extends MainRef {
  def getRef: IO[Option[SharedKillSwitch]]
  def setRef(tag: SharedKillSwitch): IO[Unit]
  def setNone: IO[Unit]
}

object ClassMyActorRef {
  def apply(myRef: Option[ActorRef] = None): IO[ClassMyActorRef] =
    for (ref <- Ref[IO].of(myRef)) yield new ClassMyActorRef(ref)
}

final class ClassMyActorRef(ref: Ref[IO, Option[ActorRef]]) extends MyActorRef {
  def getRef: IO[Option[ActorRef]]    = ref.get
  def setRef(tag: ActorRef): IO[Unit] = ref.set(Option.apply(tag))
  def setNone: IO[Unit]               = ref.set(None)
}

object MyStreemRef {
  def apply(myRef: Option[SharedKillSwitch] = None): IO[MyStreemRef] =
    for (ref <- Ref[IO].of(myRef)) yield new MyStreemRef(ref)
}

final class MyStreemRef(ref: Ref[IO, Option[SharedKillSwitch]]) extends MyStrrmRef {
  def getRef: IO[Option[SharedKillSwitch]]    = ref.get
  def setRef(tag: SharedKillSwitch): IO[Unit] = ref.set(Option.apply(tag))
  def setNone: IO[Unit]                       = ref.set(None)
}
