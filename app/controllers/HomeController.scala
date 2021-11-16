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

@Singleton
class HomeController @Inject() (
  val controllerComponents: ControllerComponents)
    extends BaseController {

  def index() =
    Action.async { implicit req =>
      //  コントローラに処理を書くのは本当は良くないよ
      // onMySQLもきっと直接書かなくてもなんとかする方法があるのだろうな
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
    Action.async { implicit req =>
      //  コントローラに処理を書くのは本当は良くないよ
      val categorysFuture: Future[Seq[TodoCategory]] = onMySQL.TodoCategoryRepository.all
      val todoOptFuture                              = onMySQL.TodoRepository.get(Todo.Id(id))

      for {
        todoOpt   <- todoOptFuture
        categorys <- categorysFuture
      } yield {
        val todo = todoOpt.getOrElse(Todo(title = "", body = None,categoryId = None))
        val vv   = HomeView(
          title = " Todo",
          cssSrc = Seq("main.css"),
          jsSrc = Seq("main.js")
        )

        Ok(views.html.Todo(vv, todo.v, categorys))
      }
    }

  def upsert(
    id: Option[Long],
    title: String,
    body: Option[String],
    categoryId: Long
  ) =
    Action { implicit req =>
      // IDがなければ新規
      // IDがあれば更新
      /*
        val todo = Todo(
          id = id,  // Option[Long] -> Option[Todo.Id] にできませんか？
          title = title,
          body = body,
          categoryId = Some(TodoCategory.Id(categoryId))
                  )
    */
      val vv = HomeView(
        title = " Todo",
        cssSrc = Seq("main.css"),
        jsSrc = Seq("main.js")
      )
      Ok(views.html.Home(vv, Nil, Nil))
    }

}
