import scala.collection.mutable

val q = mutable.Queue.empty[Int]


val result = q match {
  case x +: xs => s"$x +: $xs"
  case mutable.Queue.empty => "-"
  //case Queue(x, y, _*) => s"Queue($x, $y, _*)"
  //case Queue(x, _*) => s"Queue($x, _*)"
  //case Queue() => "Queue()"
}
