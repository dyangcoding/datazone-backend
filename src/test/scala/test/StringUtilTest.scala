package test

import org.scalatest.funsuite.AnyFunSuite
import utils.StringUtils._

class StringUtilTest extends AnyFunSuite {
  test("Or") {
    val or = Or("cat", "dog")
    assert(or == "cat OR dog")
  }

  test("combine groups with Or") {
    val or = Or("(cat has:image)", "(happiness cat)")
    assert(or == "(cat has:image) OR (happiness cat)")
  }

  test("And") {
    val and = And("cat", "dog")
    assert(and == "cat dog")
  }

  test("combine groups with And") {
    val and = And("(cat -is:retweet)", "(cat happiness)")
    assert(and == "(cat -is:retweet) (cat happiness)")
  }

  test("Group") {
    val and = Group("keyword searching should be grouped")
    assert(and == "(keyword searching should be grouped)")
  }

  test("combine groups with Group") {
    val and = Group("(cat -is:retweet) (cat happiness)")
    assert(and == "((cat -is:retweet) (cat happiness))")
  }

  test("Not") {
    val not = Not("is:retweet")
    assert(not == "-is:retweet")
  }

  test("combine group with Not") {
    val not = Not("((cat -is:retweet) (cat happiness))")
    assert(not == "-((cat -is:retweet) (cat happiness))")
  }

  test("Append At") {
    val appendAt = AppendAt("someUserId")
    assert(appendAt == "@someUserId")
  }

  test("combine group with Append At") {
    val appendAt = AppendAt("(cat -is:retweet)")
    assert(appendAt == "@(cat -is:retweet)")
  }

  test("Append Hashtag") {
    val appendHashtag = AppendHashtag("hashtag")
    assert(appendHashtag == "#hashtag")
  }

  test("combine group with Append Hashtag") {
    val appendHashtag = AppendHashtag("(hashtag -is:retweet)")
    assert(appendHashtag == "#(hashtag -is:retweet)")
  }
}
