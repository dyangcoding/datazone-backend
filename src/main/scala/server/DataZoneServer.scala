package server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path, post}

import scala.io.StdIn

/*
  ds-server responsibilities:
  1 interact with the browser:
    a) accept data to perform rule building
    b) response with tweets data to the browser
  2 interact with the ds-client:
    completing the browser request with streaming source data coming from ds client
 */
object DataZoneServer extends App {

}
