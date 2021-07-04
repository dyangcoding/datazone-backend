package db

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.bson.collection.BSONCollection
import rules.Rule

object RuleService {
  val collection: Future[BSONCollection] = DBUtil.GetCollection("rules")

  def FindById(id: String): Future[Option[Rule]] = {
    collection.flatMap(_.find(
      selector = BSONDocument("id" -> id)
    ).one[Rule])
  }

  def FindOne(rule: Rule): Future[Rule] = {
    collection.flatMap(_.find(rule).requireOne[Rule])
  }

  def InsertOne(rule: Rule): Future[WriteResult] = {
    collection.flatMap(_.update.one(
      q = BSONDocument("id" -> rule._id),
      u = BSONDocument("$set" -> rule),
      upsert = true,
    ))
  }

  def UpdateOne(rule: Rule): Future[Option[Rule]] = {
    collection.flatMap(_.findAndUpdate(
      selector = BSONDocument("id" -> rule._id),
      update = BSONDocument("$set" -> rule),
      fetchNewObject = true
    )).map(_.result[Rule])
  }

  def DeleteOne(rule: Rule): Future[WriteResult] = {
    collection.flatMap(_.delete.one(rule))
  }
}
