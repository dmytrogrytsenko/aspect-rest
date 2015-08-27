package aspect.domain

import org.joda.time.DateTime

case class PostHost(id: String)

case class PostAuthor(id: String, url: String, name: String)

case class Post(id: String,
                url: String,
                host: PostHost,
                author: PostAuthor,
                publishTime: DateTime,
                lastUpdateTime: Option[DateTime],
                title: Option[String],
                text: Option[String],
                keywords: Option[Set[String]])
