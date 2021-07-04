package rules

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import utils.JSONParser
import scala.concurrent.Future
import scala.concurrent.duration._

class RuleRoutes(buildRuleRepository: ActorRef[RuleRepository.Command])(implicit system: ActorSystem[_]) extends JsonSupport{
  import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds

  lazy val ruleRoutes: Route =
    pathPrefix("rules") {
      concat(
        post {
          entity(as[Rule]) { rule =>
            val insertedRule: Future[RuleRepository.Response] = buildRuleRepository.ask(RuleRepository.AddRule(rule, _))
            onSuccess(insertedRule) {
              case RuleRepository.ActionSucceeded(result) => {
                complete(JSONParser.toJson(result))
              }
              case RuleRepository.ActionFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
            }
          }
        },
        put {
          entity(as[Rule]) { rule =>
            val updatedRule: Future[RuleRepository.Response] = buildRuleRepository.ask(RuleRepository.UpdateRule(rule, _))
            onSuccess(updatedRule) {
              case RuleRepository.ActionSucceeded(result) => {
                complete(JSONParser.toJson(result))
              }
              case RuleRepository.ActionFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
            }
          }
        },
        delete {
          val clearRules: Future[RuleRepository.Response] = buildRuleRepository.ask(RuleRepository.ClearRules)
          onSuccess(clearRules) {
            case RuleRepository.ActionSucceeded(result) => complete(JSONParser.toJson(result))
            case RuleRepository.ActionFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
          }
        },
      )
    }
}
