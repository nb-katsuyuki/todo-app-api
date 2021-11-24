/** This is a sample of Todo Application.
  */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

import play.api.libs.json._

import play.api.libs.json.Reads._        // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import Category._
case class Category(
    id:        Option[Id],
    name:      String,
    slug:      String,
    color:     Color,
    updatedAt: LocalDateTime = NOW,
    createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Category {
  val Id = the[Identity[Id]]
  type Id         = Long @@ Category
  type WithNoId   = Entity.WithNoId[Id, Category]
  type EmbeddedId = Entity.EmbeddedId[Id, Category]

  // Color定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Color(val code: Short, val className: String)
      extends EnumStatus
  object Color extends EnumStatus.Of[Color] {
    case object FRONT   extends Color(code = 1, className = "front")
    case object BACK    extends Color(code = 2, className = "back")
    case object INFRA   extends Color(code = 3, className = "infra")
    case object UNKNOWN extends Color(code = 0, className = "unknown")

    implicit val writes: Writes[Color] = new Writes[Color] {
      def writes(color: Color) = Json.obj(
        "code"      -> color.code,
        "className" -> color.className
      )
    }
  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(
      name:  String,
      slug:  String,
      color: Color
  ): WithNoId = {
    new Entity.WithNoId(
      new Category(
        id    = None,
        name  = name,
        slug  = slug,
        color = color
      )
    )
  }

  def Empty(): WithNoId                 = {
    new Entity.WithNoId(
      new Category(
        id    = None,
        name  = "",
        slug  = "",
        color = Color.UNKNOWN
      )
    )
  }

  implicit val writes: Writes[Category] = new Writes[Category] {
    def writes(category: Category) = Json.obj(
      "id"        -> JsNumber(category.id.getOrElse(0L): Long),
      "name"      -> category.name,
      "slug"      -> category.slug,
      "color"     -> category.color.code,
      "updatedAt" -> category.updatedAt,
      "createdAt" -> category.createdAt
    )
  }
  /*
  implicit val reads: Reads[Category]   = (
    (JsPath \ "id").read[Long].map[Option[Id]](Some(Id(_))) and
      (JsPath \ "name").read[String] and
      (JsPath \ "slug").read[String] and
      (JsPath \ "color").read[Short].map(Color(_))
  )(println(_))
   */
}
