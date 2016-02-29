package aspect.domain

case class TwitterAccountId(underlying: String) extends AnyVal

case class TwitterAccount(id: TwitterAccountId,
                          email: String,
                          emailPassword: String,
                          twitterUserId: String,
                          twitterScreenName: String,
                          twitterPassword: String,
                          consumerKey: String,
                          consumerSecret: String,
                          accessToken: String,
                          accessTokenSecret: String)
