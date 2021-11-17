/**
 * to do sample project
 */

package controllers

import javax.inject._
import play.api.mvc._

import model.HomeView

import lib.model.Todo
import lib.model.TodoCategory
import lib.persistence.onMySQL

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration
import scala.concurrent.ExecutionContext.Implicits.global
// import scala.util.{Failure, Success} // Try,

import play.api.i18n._

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

@Singleton
class HomeController @Inject() (
  cc: MessagesControllerComponents,
  langs: Langs)
    extends MessagesAbstractController(cc) {

  // val availableLangs: Seq[Lang] = langs.availables
  // val title: String = messagesApi("home.title")(lang)

  val todoForm = Form(
    mapping(
      "id"         -> optional(longNumber),
      "title"      -> nonEmptyText,
      "body"       -> optional(text),
      "categoryId" -> longNumber
    )( // form -> todo
      (id, title, body, categoryId) =>
        Todo(
          id = id.map(Todo.Id(_)),
          title = title,
          body = body,
          categoryId = Some(TodoCategory.Id(categoryId))
        )
    )( // todo -> form
      (todo: Todo) => Some(todo.id, todo.title, todo.body, todo.categoryId.getOrElse(0): Long)
    )
  )

  def index() =
    Action.async { implicit req: MessagesRequest[AnyContent] =>
      //  コントローラに処理を書くのは本当は良くないよ -> とりあえず研修ではアーキテクチャは考えない
      // onMySQLもきっと直接書かなくてもなんとかする方法があるのだろうな -> DI参照
      val todosFuture: Future[Seq[Todo]]             = onMySQL.TodoRepository.all
      val categorysFuture: Future[Seq[TodoCategory]] =
        onMySQL.TodoCategoryRepository.all

      for {
        todos     <- todosFuture
        categorys <- categorysFuture
      } yield {
        val vv = HomeView(
          title = "Home",
          cssSrc = Seq("main.css"),
          jsSrc = Seq("main.js")
        )

        Ok(views.html.Home(vv, todos, categorys))
      }
    }

  // TODO詳細
  def detail(id: Long) =
    Action.async { implicit req: MessagesRequest[AnyContent] =>
      val messages: Messages                         = req.messages
      val categorysFuture: Future[Seq[TodoCategory]] = onMySQL.TodoCategoryRepository.all
      val todoOptFuture                              = onMySQL.TodoRepository.get(Todo.Id(id))
      for {
        todoOpt   <- todoOptFuture
        categorys <- categorysFuture
        // @TODO 取得失敗時動作　recover
      } yield {
        val todo = todoOpt.getOrElse(Todo(title = "", body = None, categoryId = None))
        val vv   = HomeView(
          title = " Todo",
          cssSrc = Seq("main.css"),
          jsSrc = Seq("main.js")
        )
        Ok(views.html.Todo(vv, categorys, todoForm.fill(todo.v)))
      }
    }

  def upsert() =
    Action.async { implicit req =>
      todoForm.bindFromRequest.fold(
        errors => {
          // @TOOD ここの処理はまとめたいな
          val categorysFuture: Future[Seq[TodoCategory]] = onMySQL.TodoCategoryRepository.all
          for {
            categorys <- categorysFuture
            // @TODO recover
          } yield {
            val vv = HomeView(
              title = " Todo",
              cssSrc = Seq("main.css"),
              jsSrc = Seq("main.js")
            )
            BadRequest(views.html.Todo(vv, categorys, errors))
          }
        },
        todo => {
          val todoFuture = todo.id match {
            case None => onMySQL.TodoRepository.add(Todo.NoId(todo))     // IDがなければ新規
            case _    => onMySQL.TodoRepository.update(Todo.HasId(todo)) // IDがあれば更新

          }
          for {
            res <- todoFuture
            // @TODO recover
          } yield {
            Redirect(routes.HomeController.index)
          }
        }
      )
    }
}
