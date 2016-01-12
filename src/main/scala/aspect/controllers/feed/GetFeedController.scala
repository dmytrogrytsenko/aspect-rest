package aspect.controllers.feed

import akka.actor.Props
import aspect.common.Messages.Start
import aspect.domain.{PostAuthor, Post, AccountId, PostId}
import aspect.experimental.TwitterSearcher
import aspect.experimental.TwitterSearcher.{Feed, GetFeed}
import aspect.rest.Controller
import org.joda.time.DateTime

case class PostAuthorResult(id: AccountId, url: String, name: String)

object PostAuthorResult {
  def apply(author: PostAuthor): PostAuthorResult =
    PostAuthorResult(
      id = author.id,
      url = author.url,
      name = author.name)
}

case class PostResult(id: PostId,
                      url: String,
                      author: PostAuthorResult,
                      publishTime: String,
                      title: Option[String],
                      text: Option[String])

object PostResult {
  def apply(post: Post): PostResult =
    PostResult(
      id = post.id,
      url = post.url,
      author = PostAuthorResult(post.author),
      publishTime = post.host.id.underlying,
      title = post.title,
      text = post.text)
}

case class FeedResult(posts: List[PostResult])

object GetFeedController {
  def props = Props[GetFeedController]
}

class GetFeedController extends Controller {

  def receive = {
    case Start => TwitterSearcher.endpoint ! GetFeed
    case feed: Feed => complete(FeedResult(feed.posts.map(PostResult.apply)))
  }
}
