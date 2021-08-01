package tweets

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import utils.JSONParser

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class TweetRoutes(tweetRepository: ActorRef[TweetRepository.Command])(implicit system: ActorSystem[_]) {
  import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 10.seconds

  lazy val routes: Route = {
    get {
      val fetchTweets: Future[TweetRepository.Response] = tweetRepository.ask(TweetRepository.FetchTweets)
      onSuccess(fetchTweets) {
        case TweetRepository.MultiActionsSucceeded(results) => complete(StatusCodes.OK -> JSONParser.toJson(results))
        case TweetRepository.MultiActionaFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
      }
    }
  }
}
