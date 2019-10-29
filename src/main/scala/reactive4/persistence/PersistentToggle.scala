package reactive4.persistence

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingReceive
import akka.persistence._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.actorRef2Scala


// states
sealed trait MoodState
case object Happy extends MoodState
case object Sad extends MoodState

case class MoodChangeEvent(state: MoodState)

class PersistentToggle extends PersistentActor {

  override def persistenceId = "persistent-toggle-id-1"

  def updateState(event: MoodChangeEvent): Unit =
    context.become(
      event.state match {
        case Happy => happy
        case Sad => sad
      })

  def happy: Receive = LoggingReceive {
    case "How are you?" =>
      persist(MoodChangeEvent(Sad)) {
        event =>
          updateState(event)
          sender ! "happy"
      }
    case "Done" =>
      sender ! "Done"
      context.stop(self)
  }

  def sad: Receive = LoggingReceive {
    case "How are you?" =>
      persist(MoodChangeEvent(Happy)) {
        event =>
          updateState(event)
          sender ! "sad"
      }

    case "Done" =>
      sender ! "Done"
      context.stop(self)


  }
  def receiveCommand = happy

  val receiveRecover: Receive = {
    case evt: MoodChangeEvent => updateState(evt)
  }
}

class ToggleMain extends Actor {

  val toggle = context.actorOf(Props[PersistentToggle], "toggle")

  toggle ! "How are you?"
  toggle ! "How are you?"
  toggle ! "How are you?"
  toggle ! "Done"

  def receive = LoggingReceive {

    case "Done" =>
      context.system.terminate()

    case msg: String =>
      println(s" received: $msg")

  }
}


object PersistentToggleApp extends App {
  val system = ActorSystem("Reactive4")
  val mainActor = system.actorOf(Props[ToggleMain], "mainActor")

  Await.result(system.whenTerminated, Duration.Inf)
}