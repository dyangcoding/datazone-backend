import akka.actor.typed.{ActorSystem, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{concat, path, post}
import rules.RuleRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object DataZone {
  def apply(): Behavior[Nothing] =
      Behaviors.setup[Nothing]( context => {
        new DataZone(context)
        // create all child actors here
        // val ruleSupervisor = context.spawn(RuleSupervisor(), "ruleSupervisor")
        // val tweetsSupervisor = context.spawn(TweetsSupervisor, "TweetsSupervisor")

        // watch tweets supervisor if any errors happen
        // context.watch(tweetsSupervisor)

        // do some initial setups for building rules
        // tweetsSupervisor ! TweetsSupervisor.setUpInitialRules() ...
      }
    )

  def main(args: Array[String]): Unit = {
    // Create ActorSystem and top level supervisor DataZone
    implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](DataZone(), "DataZone")
    implicit val context: ExecutionContextExecutor = system.executionContext

    // TODO: do some route settings
    val ruleRoutes = new RuleRoutes()
    val route = {
      concat(
        path("tweets") (TweetsRoutes),
        path("rules") (ruleRoutes),
      )
    }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
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
