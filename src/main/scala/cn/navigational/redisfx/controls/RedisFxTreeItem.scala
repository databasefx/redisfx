package cn.navigational.redisfx.controls

import cn.navigational.redisfx.model.RedisKey
import cn.navigational.redisfx.util.RedisDataUtil
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.{ProgressIndicator, TreeItem}

import java.util
import java.util.Collections
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
   * 获取redis原始key值
   *
   * @return
   */
  def getRowKey: String = {
    val arr = new util.ArrayList[String]()
    arr.add(this.getValue)
    var parent = this.getParent
    while (!parent.isInstanceOf[RedisDatabaseItem]) {
      arr.add(parent.getValue)
      parent = parent.getParent
    }
    Collections.reverse(arr)
    String.join(RedisDataUtil.FILE_SEPARATOR, arr)
  }

  /**
   * 根据传入的RedisKey创建子节点
   *
   * @param arr  RedisKey数组
   * @param uuid 连接缓存id
   */
  protected def createChildNode(arr: Array[RedisKey]): Unit = Platform.runLater(() => {
    getChildren.clear()
    for (item <- arr) {
      if (!item.isLeaf) {
        this.recurCreate(item, this)
      } else {
        getChildren.add(new RedisKeyTreeItem(item))
      }
    }
  })

  /**
   * 递归创建子节点
   *
   * @param uuid     redis连接缓存key
   * @param redisKey redis key
   * @param parent   父级节点
   */
  private def recurCreate(redisKey: RedisKey, parent: RedisFxTreeItem): Unit = {
    val treeItem = if (redisKey.isLeaf) {
      new RedisKeyTreeItem(redisKey)
    } else {
      new RedisFolderItem(redisKey)
    }
    parent.getChildren.add(treeItem)
    for (it <- redisKey.sub) {
      this.recurCreate(it, treeItem)
    }
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
