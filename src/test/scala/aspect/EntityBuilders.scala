package aspect

import java.util.concurrent.ThreadLocalRandom

import aspect.common._
import aspect.domain._
import aspect.domain.twitter._
import org.joda.time.DateTime

import scala.concurrent.duration.Duration

trait EntityBuilders {

  def randomInt(bound: Int) = ThreadLocalRandom.current().nextInt(bound)
  def randomLong(bound: Long) = ThreadLocalRandom.current().nextLong(bound)
  def randomBoolean = ThreadLocalRandom.current().nextBoolean()
  def randomString = newUUID
  def randomDateTime = DateTime.now.minusMinutes(randomInt(60))
  def randomDuration: Duration = Duration.fromNanos(randomLong(60000000))
  def randomOption[T](value: => T) = if (randomBoolean) Some(value) else None

  def buildTweetPoint(id: Long = randomLong(1000),
                      time: DateTime = randomDateTime) =
    TweetPoint(id, time)

  def buildTweetRange(min: TweetPoint = buildTweetPoint(),
                      max: TweetPoint = buildTweetPoint()) =
    TweetRange(min, max)

  def buildLastError(count: Int = randomInt(100),
                     message: String = randomString) =
    LastError(count, message)

  def buildLastRequest(id: TwitterSearchRequestId = TwitterSearchRequestId(randomString),
                       startTime: DateTime = randomDateTime,
                       finishTime: DateTime = randomDateTime,
                       duration: Duration = randomDuration,
                       error: Option[LastError] = randomOption(buildLastError())) =
    LastRequest(id, startTime, finishTime, duration, error)

  def buildCurrentRequest(id: TwitterSearchRequestId = TwitterSearchRequestId(randomString),
                          startTime: DateTime = randomDateTime) =
    CurrentRequest(id, startTime)

  def buildProcessingInfo(last: Option[LastRequest] = randomOption(buildLastRequest()),
                          current: Option[CurrentRequest] = randomOption(buildCurrentRequest()),
                          nextTime: DateTime = randomDateTime,
                          successInterval: Duration = randomDuration,
                          errorInterval: Duration = randomDuration) =
    ProcessingInfo(last, current, nextTime, successInterval, errorInterval)

  def buildTrackInfo(version: Long = randomLong(100),
                     createTime: DateTime = randomDateTime,
                     lastUpdateTime: DateTime = randomDateTime) =
    TrackInfo(version, createTime, lastUpdateTime)

  def buildTwitterQuery(id: TwitterQueryId = TwitterQueryId(randomString),
                        query: String = randomString,
                        found: Option[TweetRange] = randomOption(buildTweetRange()),
                        pending: Option[TweetRange] = randomOption(buildTweetRange()),
                        forward: Option[ProcessingInfo] = randomOption(buildProcessingInfo()),
                        backward: Option[ProcessingInfo] = randomOption(buildProcessingInfo()),
                        backwardCompleted: Option[Boolean] = randomOption(randomBoolean),
                        disabled: Option[Boolean] = randomOption(randomBoolean),
                        track: TrackInfo = buildTrackInfo()): TwitterQuery =
    TwitterQuery(id, query, found, pending, forward, backward, backwardCompleted, disabled, track)
}
