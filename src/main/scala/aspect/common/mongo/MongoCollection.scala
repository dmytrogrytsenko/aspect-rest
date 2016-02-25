package aspect.common.mongo

import aspect.common.Shard
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import reactivemongo.extensions.dsl.BsonDsl

import scala.concurrent.{Future, ExecutionContext}

trait MongoCollection[TId, TEntity] extends BsonDsl {

  def name: String

  def ensureIndexes(implicit db: DB, executionContext: ExecutionContext): Future[Unit] = Future { }

  implicit object ShardReader extends BSONReader[BSONInteger, Shard] {
    def read(bson: BSONInteger): Shard = Shard(bson.value)
  }

  implicit object ShardWriter extends BSONWriter[Shard, BSONInteger] {
    def write(value: Shard): BSONInteger = BSONInteger(value.underlying)
  }

  def items(implicit db: DB) = db[BSONCollection](name)

  def all(implicit db: DB,
          reader: BSONDocumentReader[TEntity],
          executionContext: ExecutionContext): Future[List[TEntity]] =
    items.find($empty).cursor[TEntity].collect[List]()

  def get(id: TId)
         (implicit db: DB,
          identityWriter: BSONWriter[TId, BSONString],
          documentReader: BSONDocumentReader[TEntity],
          executionContext: ExecutionContext): Future[Option[TEntity]] =
    items.find($id(id)).one[TEntity]

  def get(shards: Set[Shard])
         (implicit db: DB,
          reader: BSONDocumentReader[TEntity],
          executionContext: ExecutionContext): Future[List[TEntity]] =
    items.find($doc("shard" $in (shards.toSeq: _*))).cursor[TEntity].collect[List]()

  def add(entity: TEntity)
         (implicit db: DB,
          writer: BSONDocumentWriter[TEntity],
          executionContext: ExecutionContext): Future[Unit] =
    items.insert(entity).map(_ => { })

  def update(id: TId, entity: TEntity)
         (implicit db: DB,
          identityWriter: BSONWriter[TId, BSONString],
          writer: BSONDocumentWriter[TEntity],
          executionContext: ExecutionContext): Future[Unit] =
    items.update($id(id), entity).map(_ => { })

  def remove(id: TId)
            (implicit db: DB,
             identityWriter: BSONWriter[TId, BSONString],
             executionContext: ExecutionContext): Future[Unit] =
    items.remove($id(id)).map(_ => { })
}
