package reactive4.fsm

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingReceive
import akka.persistence._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.persistence.fsm._
import akka.persistence.fsm.PersistentFSM.FSMState
import scala.reflect._
import akka.actor.actorRef2Scala

 
// states 
sealed trait Mood extends FSMState
case object Happy extends Mood {
  override def identifier: String = "Happy"
}
case object Sad extends Mood {
  override def identifier: String = "Sad"
}

//commands
sealed trait Command
case object HowAreYou extends Command
case object Done extends Command

//events
case class MoodChangeEvent() 

//data
case class Data(count: Int)

class PersistentToggleFSM extends PersistentFSM [Mood, Data, MoodChangeEvent] {

  override def persistenceId = "persistent-toggle-fsm-id-1"
  override def domainEventClassTag: ClassTag[MoodChangeEvent] = classTag[MoodChangeEvent]


  startWith(Happy,Data(0))
  
  when(Happy) {
    case Event(HowAreYou,_) =>
      goto(Sad) applying MoodChangeEvent() replying Happy
    case Event(Done,_) =>
      stop replying "Done"
  } 
  
  when(Sad) {
    case Event(HowAreYou,_) =>
      goto(Happy) applying MoodChangeEvent() replying Sad
    case Event(Done,_) =>
      stop replying "Done"
  } 
  
  override def applyEvent(event: MoodChangeEvent, dataBeforeEvent: Data): Data = {
    var count: Int = dataBeforeEvent.count
    println(s" Changing from: $count to ${count+1}")
    Data(dataBeforeEvent.count + 1)
  }
  

}

class ToggleMain extends Actor {

  val toggle = context.actorOf(Props[PersistentToggleFSM], "toggle")

  toggle ! HowAreYou
  toggle ! HowAreYou
  toggle ! HowAreYou
  toggle ! Done

  def receive = LoggingReceive {

    case "Done" =>
      context.system.terminate()

    case msg: Mood =>
      println(s" received: $msg")

  }
}


object ToggleFSMApp extends App {
  val system = ActorSystem("Reactive4")
  val mainActor = system.actorOf(Props[ToggleMain], "mainActor")

  mainActor ! "Init"

  Await.result(system.whenTerminated, Duration.Inf)
}