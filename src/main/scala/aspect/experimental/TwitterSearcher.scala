package aspect.experimental

import aspect.common.actors.{ClusterSingleton, NodeSingleton, BaseActor}
import aspect.common.Messages.Start
import aspect.gateways.twitter.client.{Languages, SearchTweetsRequest, TwitterClient, TwitterAuthData}

import scala.concurrent.Await
import scala.concurrent.duration._

object TwitterSearchRequestSource extends ClusterSingleton[TwitterSearchRequestSource]

class TwitterSearchRequestSource extends BaseActor {
  def receive = ???
}

object TwitterSearcher extends NodeSingleton[TwitterSearcher]

class TwitterSearcher extends BaseActor {

  val consumerKey = "98awUmwqPUarS1hHrIKow"
  val consumerSecret = "1wvXCtRB4YbvXTgPkMOGWWRSNBmnzikBLo1f7nb1g"
  val accessTokenKey = "2412261271-io4CrltAOT9Ee9pj3iyZ1pe3h4EK2dnksO4Iw4u"
  val accessTokenSecret = "Zd1X8tcMIUdeYfaK30VDLB3AruJ7Z4tbDEUQ3uK23wdWQ"

  def receive = {
    case Start =>
      val auth = TwitterAuthData(consumerKey, consumerSecret, accessTokenKey, accessTokenSecret)
      val twitterClient = TwitterClient(auth)
      val request = SearchTweetsRequest(q = "apple", count = Some(10), lang = Some(Languages.English))
      import context.dispatcher
      val response = Await.result(twitterClient.searchTweets(request), 5.seconds)
      println("")
      println("------------------------------------------------------------------")
      response.result.fold(
        errorResult => errorResult.errors.foreach(println),
        searchTweetsResult => searchTweetsResult
          .statuses
          .map(status => s"${status.id_str} - ${status.user.screen_name} - ${status.text.take(20)}")
          .foreach(println))
      scheduleOnce(10.seconds, Start)
  }
}
