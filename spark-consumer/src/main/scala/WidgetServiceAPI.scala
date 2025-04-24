import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.mongodb.client.MongoClients
import org.bson.Document
import spray.json._
import scala.concurrent.ExecutionContextExecutor
import model.Order
import model.JsonProtocol._

object WidgetServiceAPI extends App {
  implicit val system: ActorSystem = ActorSystem("widget-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val mongoClient = MongoClients.create("mongodb://host.docker.internal:27017")
  val collection = mongoClient.getDatabase("widgetDB").getCollection("orders")

  val route =
    path("order") {
      post {
        entity(as[String]) { jsonString =>
          val order = jsonString.parseJson.convertTo[Order]
          val doc = new Document()
            .append("customerId", order.customerId)
            .append("widgetId", order.widgetId)

          collection.insertOne(doc)
          complete(StatusCodes.OK, s"Order received: $jsonString")
        }
      }
    } ~
    path("orders" / Segment) { customerId =>
      get {
        val docs = collection.find(new Document("customerId", customerId)).iterator()
        val results = scala.collection.mutable.ListBuffer[Order]()
        while (docs.hasNext) {
          val d = docs.next()
          results += Order(d.getString("customerId"), d.getString("widgetId"))
        }
        complete(results.toList.toJson.prettyPrint)
      }
    }

  Http().newServerAt("0.0.0.0", 8080).bind(route)
  println("🚀 Server online at http://localhost:8080")
}
