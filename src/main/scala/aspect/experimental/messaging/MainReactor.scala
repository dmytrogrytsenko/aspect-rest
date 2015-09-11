package aspect.experimental.messaging

import aspect.common.Messages.Start
import aspect.common.actors.NodeSingleton
import aspect.experimental.messaging.Messages.{LinkOutput, LinkInput}

object MainReactor extends NodeSingleton[MainReactor]

class MainReactor extends ReactorActor {
  def receive = {
    case Start =>
      val generator = Generator.create("generator")
      val printer = Printer.create("printer")
      val e = Exchange.create("e")
      generator.output ! LinkOutput(e)
      printer.input ! LinkInput(e)
  }
}
