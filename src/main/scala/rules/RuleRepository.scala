package rules

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

/*
  handle http actions passed down from rule routes

  note:
    1 should embedded interacting with the twitter api url: /2/tweets/search/stream/rules
    2 not sure for the moment if this should be an actor to maintain the processed rules as the internal state
    if this actor is ever going to be terminated, the previous state have to be restored.
    3
 */
object RuleRepository {
  // Trait defining successful and failure responses
  sealed trait Response

  final case class ActionSucceeded(result: PayloadEntry) extends Response
  final case class ActionFailed(reason: String) extends Response

  // Trait and its implementations representing all possible messages that can be sent to this Behavior
  sealed trait Command

  // if we only keep one existing Rule as state within the repository, the following command
  // do not make so much sense in terms of the actions it refer to, although if we
  // do need actions to valid the rule data and notify the user about it

  // check if raw rule's data which only happens in the backend server and without interaction with the Twitter API
  final case class ValidateRule(rule: Rule, replyTo: ActorRef[Response]) extends Command
  // using Twitter API to verify the incoming rule
  final case class VerifyRule(rule: Rule, replyTo: ActorRef[Response]) extends Command

  final case class AddRule(rule: Rule, replyTo: ActorRef[Response]) extends Command
  final case class GetCurrentRule(replyTo: ActorRef[Option[Rule]]) extends Command
  final case class UpdateRule(rule: Rule, replyTo: ActorRef[Response]) extends Command
  final case class ClearRules(replyTo: ActorRef[Response]) extends Command

  // This behavior handles all possible incoming messages and keeps the state in the function parameter
  def apply(currentRule: Option[Rule] = None): Behavior[Command] = Behaviors.receiveMessage {
    case ValidateRule(rule, replyTo) if rule.isValidate =>
      replyTo ! ActionSucceeded(rule.toPayload)
      Behaviors.same
  }
}
