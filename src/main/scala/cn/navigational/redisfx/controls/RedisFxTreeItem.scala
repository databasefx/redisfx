package cn.navigational.redisfx.controls

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.{ProgressIndicator, TreeItem}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 * 加载终端异常,如果当前有加载任务正在加载,再次调用该加载任务将会抛出该异常.
 *
 */
class InterruptException extends RuntimeException {

}

abstract class RedisFxTreeItem extends TreeItem[String] {
  private var defaultGra: Node = _
  private var loadStatus: Boolean = false
  private val loadGra: ProgressIndicator = new ProgressIndicator() {
    this.setPrefSize(16, 16)
  }


  /**
   * 响应刷新事件
   */
  protected def _refresh[T](func: () => T): Future[T] = {
    if (this.loadStatus) {
      return Future.failed(new InterruptException)
    }
    this.defaultGra = this.getGraphic
    this.setGraphic(loadGra)
    val promise = Promise[T]()
    Future {
      try {
        val rs = func.apply()
        promise.success(rs)
      } catch {
        case ex: Exception => promise.failure(ex)
      }
      this.loadStatus = false
      Platform.runLater(() => this.setGraphic(defaultGra))
    }
    promise.future
  }

  /**
   * 响应刷新事件
   */
  def refreshEvent(): Unit = {

  }

  /**
   * 响应删除事件
   */
  def deleteEvent(uuid: String): Future[Boolean] = {
    Future.successful(false)
  }

  /**
   * 响应创建事件
   */
  def createEvent(): Unit = {

  }
}
