package aspect.domain

import aspect.common._

case class TwitterAccountId(underlying: String) extends AnyVal

case class TwitterAccount(id: TwitterAccountId,
                          shard: Shard,
                          email: String,
                          emailPassword: String,
                          twitterUserId: String,
                          twitterScreenName: String,
                          twitterPassword: String,
                          consumerKey: String,
                          consumerSecret: String,
                          accessToken: String,
                          accessTokenSecret: String)
