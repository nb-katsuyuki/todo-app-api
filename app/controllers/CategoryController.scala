package controllers

import javax.inject._
import play.api.mvc._

import model.HomeView

// import lib.model.Todo
import lib.model.Category
import lib.persistence.onMySQL

import scala.concurrent.Future
// import scala.concurrent.Await
// import scala.concurrent.duration
import scala.concurrent.ExecutionContext.Implicits.global
// import scala.util.{Failure, Success} // Try,

import play.api.i18n._

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

@Singleton
class CategoryController @Inject() (cc: MessagesControllerComponents, langs: Langs)
    extends MessagesAbstractController(cc) {

  // val availableLangs: Seq[Lang] = langs.availables
  // val title: String = messagesApi("home.title")(lang)

  val categoryForm = Form(
    mapping(
      "id"    -> optional(longNumber),
      "name"  -> nonEmptyText,
      "slug"  -> nonEmptyText,
      "color" -> number
    )((id, name, slug, color) =>
      Category(
        id = id.map(Category.Id(_)),
        name = name,
        slug = slug,
        color = Category.Color(color.toShort)
      ) // form -> todo
    )((category: Category) => Some(category.id, category.name, category.slug, category.color.code) // todo -> form
    )
  )

  def detail(id: Long) = Action.async { implicit req: MessagesRequest[AnyContent] =>
    val categoryOptFuture = onMySQL.CategoryRepository.get(Category.Id(id))
    for {
      categoryOpt <- categoryOptFuture
    } yield {
      val vv       = HomeView(title = "Category", cssSrc = Seq("main.css"), jsSrc = Seq("main.js"))
      val category = categoryOpt.getOrElse(Category.Empty) // 新規or編集
      val colorSelect: Seq[(String, String)] = Category.Color.values.map(s => (s.code.toString, s.className))
      Ok(views.html.Category(vv, categoryForm.fill(category.v), colorSelect))
    }
  // @TODO 取得失敗時動作　recover
  }

  def upsert() = Action.async { implicit req =>
    categoryForm.bindFromRequest.fold(
      errorForm => {
        // ここの処理はまとめたいな
        val vv = HomeView(title = "Category", cssSrc = Seq("main.css"), jsSrc = Seq("main.js"))
        val colorSelect: Seq[(String, String)] = Category.Color.values.map(s => (s.code.toString, s.className))
        Future.successful(BadRequest(views.html.Category(vv, errorForm, colorSelect)))
      },
      category => {
        val categoryFuture = category.id match {
          case None => onMySQL.CategoryRepository.add(Category.NoId(category))     // IDがなければ新規
          case _    => onMySQL.CategoryRepository.update(Category.HasId(category)) // IDがあれば更新
        }

        for {
          res <- categoryFuture
        } yield {
          Redirect(routes.HomeController.index)
        }
        // @TODO recover
      }
    ) // categoryForm.bindFromRequest.fold
  } // def upsert

  def remove(id: Long) = {
    Action.async { implicit req =>
      for {
        res <- onMySQL.CategoryRepository.remove(Category.Id(id))
      } yield {
        Redirect(routes.HomeController.index)
      }
    // @TODO recover
    }
  } //   def remove(id: Long)
} // class CateogryController
