package rules

import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import db.RuleService

object RuleRepository {
  // Trait defining successful and failure responses
  sealed trait Response

  final case class ActionSucceeded(result: Rule) extends Response
  final case class ActionFailed(reason: String) extends Response

  // Trait and its implementations representing all possible messages that can be sent to this Behavior
  sealed trait Command

  final case class AddRule(rule: Rule, replyTo: ActorRef[Response]) extends Command
  final case class GetRuleById(id: String, replyTo: ActorRef[Response]) extends Command
  final case class UpdateRule(rule: Rule, replyTo: ActorRef[Response]) extends Command
  final case class ClearRules(replyTo: ActorRef[Response]) extends Command

  // This behavior handles all possible incoming messages and keeps the state in the function parameter
  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case AddRule(rule, replyTo) =>
      RuleService.InsertOne(rule).onComplete{
        case Success(writeResult) =>
          RuleService.FindOne(rule).onComplete {
            case Success(rule) => replyTo ! ActionSucceeded(rule)
            case Failure(exception) => replyTo ! ActionFailed(exception.toString)
          }
        case Failure(exception) =>
          replyTo ! ActionFailed(exception.toString)
      }
      Behaviors.same
  }
}
