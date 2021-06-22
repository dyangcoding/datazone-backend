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

  case object ActionSucceeded extends Response

  final case class ActionFailed(reason: String) extends Response

  // Trait and its implementations representing all possible messages that can be sent to this Behavior
  sealed trait Command

  final case class AddRule(rule: Rule, replyTo: ActorRef[Response]) extends Command

  final case class GetRuleById(keyword: String, replyTo: ActorRef[Option[Rule]]) extends Command

  final case class ClearRules(replyTo: ActorRef[Response]) extends Command

  // This behavior handles all possible incoming messages and keeps the state in the function parameter
  def apply(rules: Map[String, Rule] = Map.empty): Behavior[Command] = Behaviors.receiveMessage {
    case AddRule(rule, replyTo) if rules.contains(rule.keyword) =>
      replyTo ! ActionFailed("Rule already exists")
      Behaviors.same
    case AddRule(rule, replyTo) =>
      replyTo ! ActionSucceeded
      RuleRepository(rules.+(rule.keyword -> rule))
    case GetRuleById(keyword, replyTo) =>
      replyTo ! rules.get(keyword)
      Behaviors.same
    case ClearRules(replyTo) =>
      replyTo ! ActionSucceeded
      RuleRepository(Map.empty)
  }
}
