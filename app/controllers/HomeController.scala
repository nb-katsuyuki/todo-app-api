/**
 * to do sample project
 */

package controllers

import javax.inject._
import play.api.mvc._

import model.HomeView

import lib.model.Todo
import lib.model.Category
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
class HomeController @Inject() (cc: MessagesControllerComponents, langs: Langs) extends MessagesAbstractController(cc) {

  // val availableLangs: Seq[Lang] = langs.availables
  // val title: String = messagesApi("home.title")(lang)

  val todoForm = Form(
    mapping(
      "id"         -> optional(longNumber),
      "title"      -> nonEmptyText,
      "body"       -> optional(text),
      "state"      -> shortNumber,
      "categoryId" -> longNumber
    )( // form -> todo
      (id, title, body, state, categoryId) =>
        Todo(
          id = id.map(Todo.Id(_)),
          title = title,
          body = body,
          state = Todo.Status(state),
          categoryId = Some(Category.Id(categoryId))
        )
    )( // todo -> form
      (todo: Todo) =>
        Some(
          todo.id,
          todo.title,
          todo.body,
          todo.state.code,
          todo.categoryId.getOrElse(0): Long
        )
    )
  )

  def index() = Action.async { implicit req: MessagesRequest[AnyContent] =>
    //  コントローラに処理を書くのは本当は良くないよ -> とりあえず研修ではアーキテクチャは考えない
    // onMySQLもきっと直接書かなくてもなんとかする方法があるのだろうな -> DI参照
    val todosFuture: Future[Seq[Todo]]         = onMySQL.TodoRepository.all
    val categorysFuture: Future[Seq[Category]] =
      onMySQL.CategoryRepository.all

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
  def detail(id: Long) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val categorysFuture: Future[Seq[Category]] = onMySQL.CategoryRepository.all
    val todoOptFuture                          = onMySQL.TodoRepository.get(Todo.Id(id))
    for {
      todoOpt   <- todoOptFuture
      categorys <- categorysFuture
      // @TODO 取得失敗時動作　recover
    } yield {
      val todo = todoOpt.getOrElse(Todo.Empty) // 新規or編集
      val vv   = HomeView(title = "Todo", cssSrc = Seq("main.css"), jsSrc = Seq("main.js"))
      val statusSelect: Seq[(String, String)]   = Todo.Status.values.map(s => (s.code.toString, s.name))
      val categorySelect: Seq[(String, String)] = categorys.map(category => (category.id.get.toString, category.name))
      Ok(views.html.Todo(vv, todoForm.fill(todo.v), statusSelect, categorySelect))
    }
  }

  def upsert() = Action.async { implicit req =>
    todoForm.bindFromRequest.fold(
      errors => {
        // @TOOD ここの処理はまとめたいな
        val categorysFuture: Future[Seq[Category]] = onMySQL.CategoryRepository.all
        for {
          categorys <- categorysFuture
          // @TODO recover
        } yield {
          val vv = HomeView(title = "Todo", cssSrc = Seq("main.css"), jsSrc = Seq("main.js"))
          val statusSelect: Seq[(String, String)]   = Todo.Status.values.map(s => (s.code.toString, s.name))
          val categorySelect: Seq[(String, String)] =
            categorys.map(category => (category.id.get.toString, category.name))
          BadRequest(views.html.Todo(vv, errors, statusSelect, categorySelect))
        }
      },
      todo => {
        val todoFuture = todo.id match {
          case None => onMySQL.TodoRepository.add(todo.toWithNoId)      // IDがなければ新規
          case _    => onMySQL.TodoRepository.update(todo.toEmbeddedId) // IDがあれば更新
        }
        for {
          res <- todoFuture
        } yield {
          Redirect(routes.HomeController.index)
        }
        // @TODO recover
      }
    )
  } // def upsert

  def remove(id: Long) = {
    Action.async { implicit req =>
      for {
        res <- onMySQL.TodoRepository.remove(Todo.Id(id))
      } yield {
        Redirect(routes.HomeController.index)
      }
    // @TODO recover
    }
  }
}
