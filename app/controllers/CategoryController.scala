package controllers

import javax.inject._
import play.api.mvc._

import lib.model.Category
import lib.persistence.onMySQL

import scala.concurrent.Future
// import scala.concurrent.Await
// import scala.concurrent.duration
import scala.concurrent.ExecutionContext.Implicits.global
// import scala.util.{Failure, Success} // Try,

import play.api.libs.json._
import json.reads._

@Singleton
class CategoryController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {
  def color() = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      Future.successful(Ok(Json.toJson(Category.Color.values)))
  }

  def colorDetail(id: Long) = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      val res = Category.Color.values
        .find(_.code == id)
      Future.successful(Ok(Json.toJson(res)))
  }

  def index() = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      for {
        categorys <- onMySQL.CategoryRepository.all
      } yield {
        Ok(Json.toJson(categorys))
      }
  }

  def detail(id: Long) = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      val categoryOptFuture = onMySQL.CategoryRepository.get(Category.Id(id))
      for {
        categoryOpt <- categoryOptFuture
      } yield {
        val res = categoryOpt.map(category => category.v)
        Ok(Json.toJson(res))
      }
  }

  def create(id: Long) = Action.async(parse.json) { implicit req =>
    req.body
      .validate[JsValueCreateCategory]
      .fold(
        errors => {
          Future.successful(
            Ok(
              Json.obj(
                "result" -> false,
                "id"     -> id,
                "resone" -> JsError.toJson(errors)
              )
            )
          )
        },
        jsValueCategory => {
          val category = Category(
            name  = jsValueCategory.name,
            slug  = jsValueCategory.slug,
            color = Category.Color(jsValueCategory.color)
          )

          for {
            res <- onMySQL.CategoryRepository.add(category)
          } yield {
            Ok(Json.obj("result" -> true, "id" -> res.toLong))
          }
        }
      )
  } // def create(id: Long)

  def update(id: Long) = Action.async(parse.json) { implicit req =>
    req.body
      .validate[JsValueEditCategory]
      .fold(
        errors => {
          Future.successful(
            Ok(
              Json.obj(
                "result" -> false,
                "id"     -> id,
                "resone" -> JsError.toJson(errors)
              )
            )
          )
        },
        jsValueCategory => {
          val category = Category(
            id    = Some(Category.Id(jsValueCategory.id)),
            name  = jsValueCategory.name,
            slug  = jsValueCategory.slug,
            color = Category.Color(jsValueCategory.color)
          )

          for {
            res <- onMySQL.CategoryRepository.update(category.toEmbeddedId)
          } yield {
            Ok(Json.obj("result" -> true, "id" -> jsValueCategory.id))
          }
        }
      )
  } // def update(id: Long)

  def delete(id: Long) = Action.async { implicit req =>
    // トランザクション開始...どうやるのだろう
    val categoryId     = Category.Id(id)
    val removeTodos    = onMySQL.TodoRepository.removeByCategoryId(categoryId)
    val removeCategory = onMySQL.CategoryRepository.remove(categoryId)
    for {
      _ <- removeTodos
      _ <- removeCategory
    } yield {
      Ok(Json.obj("result" -> true, "id" -> id))
    }
  } //   def remove(id: Long)
} // class CateogryController
