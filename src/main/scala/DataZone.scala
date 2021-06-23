import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import rules.{RuleRepository, RuleRoutes}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object DataZone {

  sealed trait Message
  private final case class StartFailed(cause: Throwable) extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop extends Message

  def apply(host: String, port: Int): Behavior[Message] =
      Behaviors.setup ( context => {
        implicit val system: ActorSystem[Nothing] = context.system
        implicit val executionContext: ExecutionContextExecutor = system.executionContext

        val buildRuleRepository = context.spawn(RuleRepository(), "RuleRepository")
        val routes = new RuleRoutes(buildRuleRepository)

        val serverBinding: Future[Http.ServerBinding] =
          Http().newServerAt(host, port).bind(routes.ruleRoutes)

        context.pipeToSelf(serverBinding) {
          case Success(binding) =>
            val address = binding.localAddress
            system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
            Started(binding)
          case Failure(ex) =>
            system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
            system.terminate()
            StartFailed(ex)
        }

        def running(binding: ServerBinding): Behavior[Message] =
          Behaviors.receiveMessagePartial[Message] {
            case Stop =>
              context.log.info(
                "Stopping server http://{}:{}/",
                binding.localAddress.getHostString,
                binding.localAddress.getPort)
              Behaviors.stopped
          }.receiveSignal {
            case (_, PostStop) =>
              binding.unbind()
              Behaviors.same
          }

        def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
          Behaviors.receiveMessage[Message] {
            case StartFailed(cause) =>
              throw new RuntimeException("Server failed to start", cause)
            case Started(binding) =>
              context.log.info(
                "Server online at http://{}:{}/",
                binding.localAddress.getHostString,
                binding.localAddress.getPort)
              if (wasStopped) context.self ! Stop
              running(binding)
            case Stop =>
              // we got a stop message but haven't completed starting yet,
              // we cannot stop until starting has completed
              starting(wasStopped = true)
          }

        starting(wasStopped = false)
      }
    )

  def main(args: Array[String]): Unit = {
    val system: ActorSystem[DataZone.Message] =
      ActorSystem(DataZone("localhost", 8080), "DataZoneServer")
  }
}
