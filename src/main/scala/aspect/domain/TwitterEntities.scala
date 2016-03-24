package aspect.domain

import aspect.common._
import org.joda.time.DateTime

import scala.concurrent.duration.Duration

case class TwitterQueryId(underlying: String) extends AnyVal

case class TwitterRequestId(underlying: String) extends AnyVal

object TwitterRequestId {
  def generate = TwitterRequestId(newUUID)
}

case class TrackInfo(version: Long, createTime: DateTime, lastUpdateTime: DateTime)

case class TweetPoint(id: Long, time: DateTime)
case class TweetRange(min: TweetPoint, max: TweetPoint)

case class LastError(count: Int, message: String)

case class LastRequest(id: TwitterRequestId,
                       startTime: DateTime,
                       finishTime: DateTime,
                       duration: Duration,
                       error: Option[LastError])

case class CurrentRequest(id: TwitterRequestId, startTime: DateTime)

case class ProcessingInfo(last: Option[LastRequest],
                          current: Option[CurrentRequest],
                          nextTime: DateTime,
                          successInterval: Duration,
                          errorInterval: Duration) {
  def executing = current.isDefined
}

case class TwitterRequestResult(minTweetId: Long,
                                minTweetTime: DateTime,
                                maxTweetId: Long,
                                maxTweetTime: DateTime,
                                count: Int)

case object GetTwitterRequest
case object NoTwitterRequest

case class TwitterRequestCompleted(queryId: TwitterQueryId,
                                   requestId: TwitterRequestId,
                                   result: TwitterRequestResult)

case class TwitterRequestFailed(queryId: TwitterQueryId,
                                requestId: TwitterRequestId,
                                error: String)

/*
{
  "_id": "<md5 of query>",
  "query": "<query>",

  ? "userIds": [],
  ? "projectIds": [],
  ? "targetIds": [],

  "found": {
    "min": {
      "id": "<id of min found tweet>",
      "time": <time of min found tweet>
    },
    "max": {
      "id": "<id of max found tweet>",
      "maxTime": <time of max found tweet>
    }
  },

  "pending": {
    "min: {
      "id": "<id of min pending tweet>",
      "time": "<time of min pending tweet>",
    },
    "max": {
      "id": "<id of max pending tweet>",
      "time": "<time of max pending tweet>"
    }
  },

  "forward": {
    "last": {
      "id": "<correlation id of current execution if it's pending>",
      "startTime": <start time of last execution>,
      "finishTime: <start time of last execution>,
      "error": {
        "count": <amount of last errors if last operation is failed>,
        "message": "<last error message>"
      }
    },

    "nextTime": <time of next forward processing>,

    "current": {
      "id" : "<correlation id of current execution if it's pending>",
      "startTime" : <start time of current execution if it's pending>
    },


    "successInterval" : <current value of dynamic success interval>,
    "errorInterval" : <current value of dynamic error interval>

  },
  "backward": {

    "last": {
      "id": "<correlation id of current execution if it's pending>",
      "startTime": <start time of last execution>,
      "finishTime: <start time of last execution>,
      "error": {
        "count": <amount of last errors if last operation is failed>,
        "message": "<last error message>"
      }
    },

    "nextTime": <time of next backward processing>,

    "current": {
      "id": "<correlation id of current execution if it's pending>",
      "startTime": <start time of current execution if it's pending>
    },

    "errorInterval" : <current value of dynamic error interval>,

    "completed" : <indicates if the backward search completed>
  },
  "disabled" : <indicates if the query disabled>
}

*/