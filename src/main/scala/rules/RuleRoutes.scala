package rules

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import utils.JSONParser
import java.time.Instant

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class RuleRoutes(ruleRepository: ActorRef[RuleRepository.Command])(implicit system: ActorSystem[_]) extends JsonSupport{
  import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 10.seconds

  lazy val routes: Route = {
    concat(
      get {
        /**
         * no need to fetch all Rules from the Twitter API
         *  -- whenever Rules get updated (added or deleted), database would also get updated
         *  -- Twitter API contains all Rules with ID, Value (Rule as string payload), Tag,
         *      which do not match the Rules that are stored within the database
         */
        val fetchRules: Future[RuleRepository.Response] = ruleRepository.ask(RuleRepository.FetchRules)
        onSuccess(fetchRules) {
          case RuleRepository.MultiActionsSucceeded(results) => complete(StatusCodes.OK -> JSONParser.toJson(results))
          case RuleRepository.MultiActionsFailed(reason)     => complete(StatusCodes.InternalServerError -> reason)
        }
      },
      post {
        /**
         * Only add Rule to local database after Rule inserting at Twitter API was succeeded
         */
        entity(as[Rule]) { originRule =>
          val entry: PayloadEntry = PayloadBuilder(originRule).toPayloadEntry
          val payload: AddPayload = AddPayload(List(entry))
          val addingRule: Future[Rule] = for (rule <- RulesClient.addRules(payload.toJson)) yield rule
          onComplete(addingRule) {
            case Success(rule: Rule) =>
              val validatedRule = originRule.copy(id = rule.id, createdAt = Some(Instant.now().toString), payload = Some(entry.value), tag = rule.tag)
              val insertedRule: Future[RuleRepository.Response] = ruleRepository.ask(RuleRepository.AddRule(validatedRule, _))
              onSuccess(insertedRule) {
                case RuleRepository.SingleActionSucceeded(result) => complete(StatusCodes.Created -> JSONParser.toJson(result))
                case RuleRepository.SingleActionFailed(reason)    => complete(StatusCodes.InternalServerError -> reason)
              }
            case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getMessage)
          }
        }
      },
      (delete & path(LongNumber)) { id =>
        val payload: DeletePayload = DeletePayload(List(id.toString))
        val deletingRule: Future[Boolean] = for (rule <- RulesClient.deleteRules(payload.toJson)) yield rule
        onComplete(deletingRule) {
          case Success(_: Boolean) =>
            val deletedRule: Future[RuleRepository.Response] = ruleRepository.ask(RuleRepository.DeleteRuleById(id.toString, _))
            onSuccess(deletedRule) {
              case RuleRepository.ActionSucceeded()    => complete(StatusCodes.OK)
              case RuleRepository.ActionFailed(reason) => complete(StatusCodes.InternalServerError -> reason)
            }
          case Failure(exception) => complete(StatusCodes.BadRequest -> exception.getMessage)
        }
      },
    )
  }
}
