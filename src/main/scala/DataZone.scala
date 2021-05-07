import akka.actor.typed.{ActorSystem, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

object DataZone {
  def apply(): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => new DataZone(context))

  def main(args: Array[String]): Unit = {
    // Create ActorSystem and top level supervisor
    ActorSystem[Nothing](DataZone(), "DataZone")
  }
}

class DataZone(context: ActorContext[Nothing]) extends AbstractBehavior[Nothing](context) {
  context.log.info("DataZone Application started")

  override def onMessage(msg: Nothing): Behavior[Nothing] = {
    // No need to handle any messages
    Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[Nothing]] = {
    case PostStop =>
      context.log.info("DataZone Application stopped")
      this
  }
}
