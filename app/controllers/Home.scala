/**
 * to do sample project
 */

package controllers

import javax.inject._
import play.api.mvc._
import model.ViewValueHome

import lib.model.Todo
import lib.model.TodoCategory
import lib.persistence.onMySQL

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration
import scala.concurrent.ExecutionContext.Implicits.global
// import scala.util.{Failure, Success} // Try,

@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def index() =
    Action { implicit req =>
      //  コントローラに処理を書くのは本当は良くないよ
      val todosFuture: Future[Seq[Todo]] = onMySQL.TodoRepository.all
      val categorysFuture: Future[Seq[TodoCategory]] =
        onMySQL.TodoCategoryRepository.all

      val todos: Seq[Todo] = Await.result(todosFuture, duration.Duration.Inf)
      val categorys: Seq[TodoCategory] =
        Await.result(categorysFuture, duration.Duration.Inf)

      val vv = ViewValueHome(
        title = "Home",
        cssSrc = Seq("main.css"),
        jsSrc = Seq("main.js")
      )

      Ok(views.html.Home(vv, todos, categorys))
    }
/*
  // TODO詳細
  def detail(id: Int) = {
    Action { implicit req =>
      //  コントローラに処理を書くのは本当は良くないよ
      val todoFuture: Future[Option[Todo]] = onMySQL.TodoRepository.get(Todo.Id(id))
      val categorysFuture: Future[Seq[TodoCategory]] =
        onMySQL.TodoCategoryRepository.all

      val todo: Option[Todo] = Await.result(todoFuture, duration.Duration.Inf)
      val categorys: Seq[TodoCategory] =
        Await.result(categorysFuture, duration.Duration.Inf)

      val vv = ViewValueHome(
        title = " Todo詳細",
        cssSrc = Seq("main.css"),
        jsSrc = Seq("main.js")
      )
      Ok(views.html.Todo(vv, todo.get, categorys))
    }
  }
  */
}
