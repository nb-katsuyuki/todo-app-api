/**
 * This is a sample of Todo Application.
 */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
import Todo._
case class Todo(
    id: Option[Id],
    categoryId: Option[Category.Id], // @TODO とりあえず
    title: String,
    body: Option[String],
    state: Status = Todo.Status.PENDING,
    updatedAt: LocalDateTime = NOW,
    createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Todo {
  val Id = the[Identity[Id]]
  type Id         = Long @@ Todo
  type WithNoId   = Entity.WithNoId[Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]

  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status                                                   extends EnumStatus.Of[Status] {
    case object PENDING   extends Status(code = 0, name = "未着手")
    case object RUNNING   extends Status(code = 1, name = "進行中")
    case object COMPLETED extends Status(code = 2, name = "完了")
  }

  // INSERT時のIDがAutoincrementのため,IDなしであることを示すオブジェクトに変換
  def apply(
      title: String,
      body: Option[String],
      categoryId: Option[Category.Id]
  ): WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id = None,
        categoryId = categoryId,
        title = title,
        body = body
      )
    )
  }

  def Empty(): WithNoId = {
    new Entity.WithNoId(
      new Todo(
        id = None,
        title = "",
        body = None,
        categoryId = None
      )
    )
  }

}
