package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueEditCategory(
    id:    Long,
    name:  String,
    slug:  String,
    color: Short
)

object JsValueEditCategory {
  implicit val reads: Reads[JsValueEditCategory] =
    Json.reads[JsValueEditCategory]
}
