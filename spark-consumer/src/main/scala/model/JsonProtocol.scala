package model

import spray.json._
import model.Order

object JsonProtocol extends DefaultJsonProtocol {
  implicit val orderFormat: RootJsonFormat[Order] = jsonFormat2(Order)
}
