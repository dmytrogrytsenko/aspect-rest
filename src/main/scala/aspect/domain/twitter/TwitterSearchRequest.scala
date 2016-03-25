package aspect.domain.twitter

import aspect.common._

case class TwitterSearchRequestId(underlying: String) extends AnyVal

object TwitterSearchRequestId {
  def generate = TwitterSearchRequestId(newUUID)
}

case class TwitterSearchRequest(id: TwitterSearchRequestId,
                                query: String,
                                minTweetId: Option[Long] = None,
                                maxTweetId: Option[Long] = None)

object TwitterSearchRequest {
  def pending(query: TwitterQuery) =
    TwitterSearchRequest(TwitterSearchRequestId.generate, query.query,
      minTweetId = Some(query.found.get.max.id + 1),
      maxTweetId = Some(query.pending.get.min.id - 1))

  def forward(query: TwitterQuery) =
    TwitterSearchRequest(TwitterSearchRequestId.generate, query.query,
      minTweetId = Some(query.found.get.max.id + 1))

  def backward(query: TwitterQuery) =
    TwitterSearchRequest(TwitterSearchRequestId.generate, query.query,
      maxTweetId = Some(query.found.get.min.id - 1))
}

