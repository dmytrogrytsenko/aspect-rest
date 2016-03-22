package aspect

import java.util.concurrent.ThreadLocalRandom

import aspect.common._
import aspect.domain._
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

  def buildTweetPoint(id: String = randomString,
                      time: DateTime = randomDateTime) =
    TweetPoint(id, time)

  def buildTweetRange(min: TweetPoint = buildTweetPoint(),
                      max: TweetPoint = buildTweetPoint()) =
    TweetRange(min, max)

  def buildLastError(count: Int = randomInt(100),
                     message: String = randomString) =
    LastError(count, message)

  def buildLastExecution(id: TwitterExecutionId = TwitterExecutionId(randomString),
                         startTime: DateTime = randomDateTime,
                         finishTime: DateTime = randomDateTime,
                         duration: Duration = randomDuration,
                         error: Option[LastError] = randomOption(buildLastError())) =
    LastExecution(id, startTime, finishTime, duration, error)

  def buildCurrentExecution(id: TwitterExecutionId = TwitterExecutionId(randomString),
                            startTime: DateTime = randomDateTime) =
    CurrentExecution(id, startTime)

  def buildProcessingInfo(last: Option[LastExecution] = randomOption(buildLastExecution()),
                          current: Option[CurrentExecution] = randomOption(buildCurrentExecution()),
                          nextTime: DateTime = randomDateTime,
                          successInterval: Duration = randomDuration,
                          errorInterval: Duration = randomDuration) =
    ProcessingInfo(last, current, nextTime, successInterval, errorInterval)

  def buildTrackInfo(version: Long = randomLong(100),
                     createTime: DateTime = randomDateTime,
                     lastUpdateTime: DateTime = randomDateTime) =
    TrackInfo(version, createTime, lastUpdateTime)

  def buildTwitterRequest(id: TwitterRequestId = TwitterRequestId(randomString),
                          query: String = randomString,
                          found: Option[TweetRange] = randomOption(buildTweetRange()),
                          pending: Option[TweetRange] = randomOption(buildTweetRange()),
                          forward: Option[ProcessingInfo] = randomOption(buildProcessingInfo()),
                          backward: Option[ProcessingInfo] = randomOption(buildProcessingInfo()),
                          backwardCompleted: Option[Boolean] = randomOption(randomBoolean),
                          disabled: Option[Boolean] = randomOption(randomBoolean),
                          track: TrackInfo = buildTrackInfo()): TwitterRequest =
    TwitterRequest(id, query, found, pending, forward, backward, backwardCompleted, disabled, track)
}
