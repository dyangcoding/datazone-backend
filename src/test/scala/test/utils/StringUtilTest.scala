package test.utils

import org.scalatest.funsuite.AnyFunSuite
import utils.StringUtils._

class StringUtilTest extends AnyFunSuite {
  test("Combine operators with 'Or'") {
    val or = Or("cat", "dog")
    assert(or == "cat OR dog")
  }

  test("combine operator groups with 'Or'") {
    val or = Or("(cat has:image)", "(happiness cat)")
    assert(or == "(cat has:image) OR (happiness cat)")
  }

  test("combine operators with 'And'") {
    val and = And("cat", "dog")
    assert(and == "cat dog")
  }

  test("combine operator groups with 'And'") {
    val and = And("(cat -is:retweet)", "(cat happiness)")
    assert(and == "(cat -is:retweet) (cat happiness)")
  }

  test("grouping operator") {
    val and = Group("keyword searching should be grouped")
    assert(and == "(keyword searching should be grouped)")
  }

  test("combine groups with 'Group'") {
    val and = Group("(cat -is:retweet) (cat happiness)")
    assert(and == "((cat -is:retweet) (cat happiness))")
  }

  test("Not") {
    val not = Not("is:retweet")
    assert(not == "-is:retweet")
  }

  test("combine group with 'Not'") {
    val not = Not("((cat -is:retweet) (cat happiness))")
    assert(not == "-((cat -is:retweet) (cat happiness))")
  }

  test("append 'At'") {
    val appendAt = AppendAt("someUserId")
    assert(appendAt == "@someUserId")
  }

  test("combine group with 'AppendAt'") {
    val appendAt = AppendAt("(cat -is:retweet)")
    assert(appendAt == "@(cat -is:retweet)")
  }

  test("append Hashtag") {
    val appendHashtag = AppendHashtag("hashtag")
    assert(appendHashtag == "#hashtag")
  }

  test("combine group with 'AppendHashtag'") {
    val appendHashtag = AppendHashtag("(hashtag -is:retweet)")
    assert(appendHashtag == "#(hashtag -is:retweet)")
  }
}
