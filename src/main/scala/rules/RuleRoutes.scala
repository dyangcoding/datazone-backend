package rules

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

/*
dummy route to handle all possible actions related to rules, that are retrieving existing rules, updating rules
the actual interact with the twitter api happens by ruleRepository
 */
class RuleRoutes(buildRuleRepository: ActorRef[RuleRepository.Command])(implicit system: ActorSystem[_]) extends SprayJsonSupport{
  import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds

  lazy val ruleRoutes: Route =
    concat(
      post {
        // TODO: need to unmarshall the rule object
        entity(as[RuleRepository.Rule]) { rule =>
          val updatedRule: Future[RuleRepository.Response] = buildRuleRepository.ask(RuleRepository.AddRule(rule, _))
          onSuccess(updatedRule) {
            case RuleRepository.ActionSucceeded => complete("Rule updated")
            case RuleRepository.ActionFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
          }
        }
      },
      delete {
        val clearRules: Future[RuleRepository.Response] = buildRuleRepository.ask(RuleRepository.ClearRules)
        onSuccess(clearRules) {
          case RuleRepository.ActionSucceeded => complete("Rule cleared")
          case RuleRepository.ActionFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
        }
      },
      (get & path(LongNumber)) { id =>
        val retrieveRules: Future[Option[RuleRepository.Rule]] = buildRuleRepository.ask(RuleRepository.GetRuleById(id, _))
        rejectEmptyResponse {
          complete(retrieveRules)
        }
      }
    )
}
