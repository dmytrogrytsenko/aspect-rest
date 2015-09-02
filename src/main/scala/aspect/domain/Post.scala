package aspect.domain

import org.joda.time.DateTime

case class PostId(underlying: String) extends AnyVal

case class PostHost(id: HostId)

case class PostAuthor(id: AccountId, url: String, name: String)

case class Post(id: PostId,
                url: String,
                host: PostHost,
                author: PostAuthor,
                publishTime: DateTime,
                lastUpdateTime: Option[DateTime],
                title: Option[String],
                text: Option[String],
                keywords: Option[Set[String]])
