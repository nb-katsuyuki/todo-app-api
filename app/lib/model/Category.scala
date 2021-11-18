/**
 * This is a sample of Todo Application.
 */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import Category._
case class Category(
    id: Option[Id],
    name: String,
    slug: String,
    color: Color,
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
  sealed abstract class Color(val code: Short, val className: String) extends EnumStatus
  object Color                                                        extends EnumStatus.Of[Color] {
    case object FRONT   extends Color(code = 1, className = "front")
    case object BACK    extends Color(code = 2, className = "back")
    case object INFRA   extends Color(code = 3, className = "infra")
    case object UNKNOWN extends Color(code = 0, className = "unknown")
  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(
      name: String,
      slug: String,
      color: Color
  ): WithNoId = {
    new Entity.WithNoId(
      new Category(
        id = None,
        name = name,
        slug = slug,
        color = color
      )
    )
  }

  def Empty(): WithNoId = {
    new Entity.WithNoId(
      new Category(
        id = None,
        name = "",
        slug = "",
        color = Color.UNKNOWN
      )
    )
  }
}
