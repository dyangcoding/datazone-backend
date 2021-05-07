package client

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

// interact with the twitter API and provide tweets as source to the data zone server
// TODO: more later on
object DataZoneClient extends App {
  implicit val system = ActorSystem(Behaviors.empty, "dz-client")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext
}
