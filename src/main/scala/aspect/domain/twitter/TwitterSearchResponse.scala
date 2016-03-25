package aspect.domain.twitter

import org.joda.time.DateTime

case class TwitterSearchResponse(requestId: TwitterSearchRequestId,
                                 minTweetId: Long,
                                 minTweetTime: DateTime,
                                 maxTweetId: Long,
                                 maxTweetTime: DateTime,
                                 count: Int)
