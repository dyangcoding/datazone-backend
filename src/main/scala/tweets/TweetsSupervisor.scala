package tweets

import akka.{NotUsed, actor}
import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import akka.util.ByteString
import spray.json.RootJsonFormat

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

// interact with the twitter API and provide tweets as source to the data zone server
// TODO: more later on
class TweetsSupervisor extends Actor with ActorLogging{
  import akka.pattern.pipe
  import context.dispatcher

  implicit val system: actor.ActorSystem = context.system
  val http: HttpExt = Http(system)

  case class Tweet(uid: Int, txt: String)

  // marshal to and from a stream Source[T, _]
  object TweetJsonProtocol
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
      with spray.json.DefaultJsonProtocol {

    implicit val tweetFormat: RootJsonFormat[Tweet] = jsonFormat2(Tweet.apply)
  }

  // [2] pick a Source rendering support trait:
  // Note that the default support renders the Source as JSON Array
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport.json()

  override def preStart(): Unit = {
    http.singleRequest(HttpRequest(uri = "http://akka.io"))
      .pipeTo(self)
  }

  val response: Future[HttpResponse] =
    RestartSource
      .withBackoff(
        minBackoff = 10 milliseconds,
        maxBackoff = 30 seconds,
        randomFactor = 0.2,
        maxRestarts = 10
      ) { () =>
        val responseFuture: Future[HttpResponse] =
          Http().singleRequest(HttpRequest(uri = "http://localhost:8080/"))

        Source
          .future(responseFuture)
          .mapAsync(parallelism = 1) {
            case HttpResponse(StatusCodes.OK, _, entity, _) =>
              Unmarshal(entity.dataBytes).to[Tweet]
            case HttpResponse(StatusCodes.InternalServerError, _, _, _) =>
              throw Throwable
            case HttpResponse(statusCode, _, _, _) =>
              throw Throwable
          }
      }


  def receive: Receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes
        .runFold(ByteString(""))(_ ++ _).foreach { body =>
        Unmarshal(body).to[Tweet]
        log.info("Got response, body: " + body.utf8String)
      }
    case resp @ HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }

}
