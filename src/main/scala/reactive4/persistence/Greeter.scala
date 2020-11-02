package reactive4.persistence

import akka.persistence.typed.PersistenceId
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.persistence.typed.scaladsl.Effect
import akka.actor.typed.ActorRef

object Greeter {

  sealed trait Command
  case class Greet(name: String) extends Command
  case class GetAllGreeted(replyTo: ActorRef[List[String]]) extends Command

  sealed trait Event
  case class Greeted(name: String) extends Event

  def apply(persistenceId: PersistenceId): Behavior[Command] =
    Behaviors.setup { context =>
      EventSourcedBehavior[Command, Event, List[String]](
        persistenceId,
        List.empty,
        commandHandler(context),
        eventHandler()
      )
    }

  private def commandHandler(context: ActorContext[Command]): (List[String], Command) => Effect[Event, List[String]] = {
    case (_, Greet(name)) =>
      context.log.info(s"Hello $name!")
      Effect.persist(Greeted(name))
    case (state, GetAllGreeted(replyTo)) =>
      Effect.reply(replyTo)(state)
  }

  private def eventHandler(): (List[String], Event) => List[String] = {
    case (state, Greeted(name)) => state :+ name
  }
}
