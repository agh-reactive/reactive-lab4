package reactive4.persistence

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object PersistentToggle {
  trait Command
  case class HowAreYou(relyTo: ActorRef[String]) extends Command
  case class Done(relyTo: ActorRef[String])      extends Command

  sealed trait State
  case object Happy extends State
  case object Sad   extends State

  trait Event
  case class MoodChanged(state: State) extends Event

  def apply(persistenceId: PersistenceId): Behavior[Command] = EventSourcedBehavior[Command, Event, State](
    persistenceId = persistenceId,
    emptyState = Happy,
    commandHandler = commandHandler,
    eventHandler = eventHandler
  )

  private val commandHandler: (State, Command) => Effect[Event, State] = { (state, command) =>
    state match {
      case Happy =>
        command match {
          case HowAreYou(replyTo) =>
            replyTo ! "happy"
            Effect.persist(MoodChanged(Sad))
          case Done(replyTo) =>
            replyTo ! "Done"
            Effect.none
        }
      case Sad =>
        command match {
          case HowAreYou(replyTo) =>
            replyTo ! "sad"
            Effect.persist(MoodChanged(Happy))
          case Done(replyTo) =>
            replyTo ! "Done"
            Effect.none
        }
    }
  }

  private val eventHandler: (State, Event) => State = { (state, event) =>
    state match {
      case Happy => Sad
      case Sad   => Happy
    }
  }
}

object ToggleMain {

  def apply(): Behavior[String] = Behaviors.setup { context =>
    apply(context.spawn(PersistentToggle(PersistenceId.ofUniqueId("toggle")), "toggle").ref)
  }

  def apply(toggle: ActorRef[PersistentToggle.Command]): Behavior[String] =
    Behaviors.receive(
      (context, msg) =>
        msg match {
          case "Init" =>
            toggle ! PersistentToggle.HowAreYou(context.self)
            toggle ! PersistentToggle.HowAreYou(context.self)
            toggle ! PersistentToggle.HowAreYou(context.self)
            toggle ! PersistentToggle.Done(context.self)
            Behaviors.same
          case "Done" =>
            context.system.terminate
            Behaviors.stopped
          case msg: String =>
            println(s" received: $msg")
            Behaviors.same
      }
    )
}

object PersistentToggleApp extends App {
  val system: ActorSystem[String] = ActorSystem(ToggleMain(), "mainActor")

  system ! "Init"

  Await.result(system.whenTerminated, Duration.Inf)
}
