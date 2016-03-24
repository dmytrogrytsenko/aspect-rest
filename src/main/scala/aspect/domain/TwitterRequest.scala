package aspect.domain

import org.joda.time.DateTime

case class TwitterRequest(id: TwitterRequestId,
                          query: String,
                          minTweetId: Option[Long] = None,
                          maxTweetId: Option[Long] = None,
                          startTime: DateTime = DateTime.now)

object TwitterRequest {
  def pending(query: TwitterQuery) =
    TwitterRequest(TwitterRequestId.generate, query.query,
      minTweetId = Some(query.found.get.max.id + 1),
      maxTweetId = Some(query.pending.get.min.id - 1))

  def forward(query: TwitterQuery) =
    TwitterRequest(TwitterRequestId.generate, query.query,
      minTweetId = Some(query.found.get.max.id + 1))

  def backward(query: TwitterQuery) =
    TwitterRequest(TwitterRequestId.generate, query.query,
      maxTweetId = Some(query.found.get.min.id - 1))
}

