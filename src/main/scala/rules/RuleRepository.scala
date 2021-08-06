package rules

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import db.RuleService

/*
  responsible for accessing Rules from database
 */
object RuleRepository {
  // Trait defining successful and failure responses
  sealed trait Response

  final case class ActionSucceeded() extends Response
  final case class SingleActionSucceeded(result: Rule) extends Response
  final case class MultiActionsSucceeded(results: Seq[Rule]) extends Response
  final case class ActionFailed(reason: String) extends Response
  final case class SingleActionFailed(reason: String) extends Response
  final case class MultiActionsFailed(reason: String) extends Response

  // Trait and its implementations representing all possible messages that can be sent to this Behavior
  sealed trait Command

  final case class FetchRules(replyTo: ActorRef[Response]) extends Command
  final case class AddRule(rule: Rule, replyTo: ActorRef[Response]) extends Command
  final case class GetRuleById(id: String, replyTo: ActorRef[Response]) extends Command
  final case class DeleteRuleById(id: String, replyTo: ActorRef[Response]) extends Command
  final case class ClearRules(replyTo: ActorRef[Response]) extends Command

  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case FetchRules(replyTo) =>
      RuleService.FetchAll().onComplete{
        case Success(writeResult) =>
          replyTo ! MultiActionsSucceeded(writeResult)
        case Failure(exception) =>
          replyTo ! MultiActionsFailed(exception.getMessage)
      }
      Behaviors.same

    case AddRule(rule, replyTo) =>
      RuleService.InsertOne(rule).onComplete{
        case Success(_) =>
          RuleService.FindOne(rule).onComplete {
            case Success(rule) => replyTo ! SingleActionSucceeded(rule)
            case Failure(exception) => replyTo ! SingleActionFailed(exception.toString)
          }
        case Failure(exception) =>
          replyTo ! SingleActionFailed(exception.getMessage)
      }
      Behaviors.same

    case DeleteRuleById(id, replyTo) =>
      RuleService.DeleteById(id).onComplete {
        case Success(_) =>
          replyTo ! ActionSucceeded()
        case Failure(exception) =>
          replyTo ! ActionFailed(exception.getMessage)
      }
      Behaviors.same
  }
}