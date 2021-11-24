package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueEditTodo(
    id:         Long,
    categoryId: Long,
    title:      String,
    body:       Option[String],
    state:      Short
)

object JsValueEditTodo {
  implicit val reads: Reads[JsValueEditTodo] = Json.reads[JsValueEditTodo]
}
