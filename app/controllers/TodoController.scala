package controllers

import javax.inject._
import play.api.mvc._

import lib.model.Todo
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
class TodoController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  def status() = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      Future.successful(Ok(Json.toJson(Todo.Status.values)))
  }

  def statusDetail(id: Long) = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      val res = Todo.Status.values.find(_.code == id)
      Future.successful(Ok(Json.toJson(res)))
  }

  def index()          = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      for {
        todos <- onMySQL.TodoRepository.all
      } yield {
        Ok(Json.toJson(todos))
      }
  }
  def detail(id: Long) = Action.async {
    implicit req => //: MessagesRequest[AnyContent] =>
      val todoOptFuture = onMySQL.TodoRepository.get(Todo.Id(id))
      for {
        todoOpt <- todoOptFuture
      } yield {
        val res = todoOpt.map(todo => todo.v)
        Ok(Json.toJson(res))
      }
  }

  def create(id: Long) = Action.async(parse.json) { implicit req =>
    req.body
      .validate[JsValueCreateTodo]
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
        jsValueTodo => {
          val todo = Todo(
            categoryId = Some(Category.Id(jsValueTodo.categoryId)),
            title      = jsValueTodo.title,
            body       = jsValueTodo.body
          )

          for {
            res <- onMySQL.TodoRepository.add(todo)
          } yield {
            Ok(Json.obj("result" -> true, "id" -> res.toLong))
          }
        }
      )
  } // def create(id: Long)

  def update(id: Long) = Action.async(parse.json) { implicit req =>
    req.body
      .validate[JsValueEditTodo]
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
        jsValueTodo => {
          val todo = Todo(
            id         = Some(Todo.Id(jsValueTodo.id)),
            categoryId = Some(Category.Id(jsValueTodo.categoryId)),
            title      = jsValueTodo.title,
            body       = jsValueTodo.body,
            state      = Todo.Status(jsValueTodo.state)
          )

          for {
            res <- onMySQL.TodoRepository.update(todo.toEmbeddedId)
          } yield {
            Ok(Json.obj("result" -> true, "id" -> jsValueTodo.id))
          }
        }
      )
  } // def update(id: Long)

  def delete(id: Long) = Action.async { implicit req =>
    val todoId = Todo.Id(id)
    for {
      _ <- onMySQL.TodoRepository.remove(todoId)
    } yield {
      Ok(Json.obj("result" -> true, "id" -> id))
    }
  } //   def remove(id: Long)

} // class CateogryController
