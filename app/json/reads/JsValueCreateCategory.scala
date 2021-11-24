package json.reads

import play.api.libs.json.{Json, Reads}

case class JsValueCreateCategory(
    name:  String,
    slug:  String,
    color: Short
)

object JsValueCreateCategory {
  implicit val reads: Reads[JsValueCreateCategory] =
    Json.reads[JsValueCreateCategory]
}
