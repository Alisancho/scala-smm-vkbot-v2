package ru.helper
import akka.actor.Actor
import akka.event.Logging

trait MyActor extends Actor {
  implicit val log = Logging(context.system, this)
}
