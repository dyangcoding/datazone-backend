package tweets

import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import db.TweetService

import scala.util.{Failure, Success}

object TweetRepository {
  // Trait defining successful and failure responses
  sealed trait Response

  final case class MultiActionsSucceeded(results: Seq[Tweet]) extends Response
  final case class MultiActionaFailed(reason: String) extends Response

  // Trait and its implementations representing all possible messages that can be sent to this Behavior
  sealed trait Command

  final case class FetchTweets(replyTo: ActorRef[Response]) extends Command

  // This behavior handles all possible incoming messages and keeps the state in the function parameter
  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case FetchTweets(replyTo) =>
      TweetService.FetchAll().onComplete{
        case Success(writeResult) =>
          replyTo ! MultiActionsSucceeded(writeResult)
        case Failure(exception) =>
          replyTo ! MultiActionaFailed(exception.toString)
      }
      Behaviors.same
  }
}
