package aspect.experimental

import aspect.common._
import aspect.common.actors.{NodeSingleton, BaseActor}
import aspect.common.Messages.Start
import aspect.domain._
import aspect.experimental.TwitterSearcher.{Feed, GetFeed}
import aspect.gateways.twitter.client._
import org.joda.time.DateTime

import scala.concurrent.duration._

object TwitterSearcher extends NodeSingleton[TwitterSearcher] {
  case object GetFeed
  case class Feed(posts: List[Post])
}

class TwitterSearcher extends BaseActor {

  val consumerKey = "98awUmwqPUarS1hHrIKow"
  val consumerSecret = "1wvXCtRB4YbvXTgPkMOGWWRSNBmnzikBLo1f7nb1g"
  val accessTokenKey = "2412261271-io4CrltAOT9Ee9pj3iyZ1pe3h4EK2dnksO4Iw4u"
  val accessTokenSecret = "Zd1X8tcMIUdeYfaK30VDLB3AruJ7Z4tbDEUQ3uK23wdWQ"

  lazy val auth = TwitterAuthData(consumerKey, consumerSecret, accessTokenKey, accessTokenSecret)
  lazy val twitterClient = TwitterClient(auth)
  lazy val request = SearchTweetsRequest(q = "apple", count = Some(10), lang = Some(Languages.English))

  case object Iterate

  private var posts: List[Post] = List.empty


  def receive = {
    case Start =>
      schedule(10.seconds, Iterate)
    case Iterate =>
      import context.dispatcher
      twitterClient.searchTweets(request).pipeTo(self)
    case response: SearchTweetsResponse =>
      posts = response.result.fold(
        errorResult => List.empty,
        searchTweetsResult => searchTweetsResult.statuses.map(status => buildPost(status)))
    case GetFeed =>
      sender ! Feed(posts)
  }

  def buildPost(status: TwitterStatus): Post =
    Post(
      id = PostId(status.id_str),
      url = s"https://twitter.com/${status.user.screen_name}/status/${status.id_str}",
      host = PostHost(HostId(status.created_at)),
      author = PostAuthor(
        id = AccountId(status.user.id_str),
        url = s"https://twitter.com/${status.user.screen_name}",
        name = status.user.name),
      publishTime = DateTime.now, //status.created_at,
      lastUpdateTime = None,
      title = None,
      text = Some(status.text))
}
