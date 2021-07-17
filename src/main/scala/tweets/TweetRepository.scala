package tweets

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import db.TweetService

import scala.util.{Failure, Success}

object TweetRepository {
  // Trait defining successful and failure responses
  sealed trait Response

  final case class ActionSucceededMany(results: Seq[Tweet]) extends Response
  final case class ActionFailed(reason: String) extends Response

  // Trait and its implementations representing all possible messages that can be sent to this Behavior
  sealed trait Command

  final case class FetchTweets(replyTo: ActorRef[Response]) extends Command

  // This behavior handles all possible incoming messages and keeps the state in the function parameter
  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case FetchTweets(replyTo) =>
      TweetService.FetchAll().onComplete{
        case Success(writeResult) =>
          replyTo ! ActionSucceededMany(writeResult)
        case Failure(exception) =>
          replyTo ! ActionFailed(exception.toString)
      }
      Behaviors.same
  }
}
