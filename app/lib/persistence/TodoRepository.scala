/**
 * This is a sample of Todo Application.
 */

package lib.persistence

import scala.concurrent.Future
import ixias.persistence.SlickRepository
import lib.model.Todo
import lib.model.Category
import slick.jdbc.JdbcProfile

// TodoRepository: TodoTableへのクエリ発行を行うRepository層の定義
//~~~~~~~~~~~~~~~~~~~~~~
case class TodoRepository[P <: JdbcProfile]()(implicit val driver: P)
    extends SlickRepository[Todo.Id, Todo, P]
    with db.SlickResourceProvider[P] {

  import api._

  def all(): Future[Seq[Todo]] = RunDBAction(TodoTable, "slave") { _.result }

  def get(id: Id): Future[Option[EntityEmbeddedId]] = RunDBAction(TodoTable, "slave") {
    _.filter(_.id === id).result.headOption
  }

  def add(entity: EntityWithNoId): Future[Id] = RunDBAction(TodoTable) { slick =>
    slick returning slick.map(_.id) += entity.v
  }

  def update(entity: EntityEmbeddedId): Future[Option[EntityEmbeddedId]] = RunDBAction(TodoTable) { slick =>
    val row = slick.filter(_.id === entity.id)
    for {
      old <- row.result.headOption
      _   <- old match {
        case None    => DBIO.successful(0)
        case Some(_) => row.update(entity.v)
      }
    } yield old
  }

  def remove(id: Id): Future[Option[EntityEmbeddedId]] = RunDBAction(TodoTable) { slick =>
    val row = slick.filter(_.id === id)
    for {
      old <- row.result.headOption
      _   <- old match {
        case None    => DBIO.successful(0)
        case Some(_) => row.delete
      }
    } yield old
  }

  def removeByCategoryId(categoryId: Category.Id): Future[Seq[EntityEmbeddedId]] = RunDBAction(TodoTable) { slick =>
    val rows = slick.filter(_.categoryId === categoryId)
    for {
      old <- rows.result
      _   <- old match {
        case Nil => DBIO.successful(0)
        case _   => rows.delete
      }
    } yield old
  }
}
