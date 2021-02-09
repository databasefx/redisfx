package cn.navigational.redisfx.controls

import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controller.pane.{RedisClientTabPaneController, RedisValTabController}
import javafx.application.Platform
import javafx.scene.control.{Tab, TreeItem}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class RedisValTab(val redisKey: String, val uuid: String, val index: Int) extends Tab {
  private val controller: RedisValTabController = new RedisValTabController(this)
  var treeItem: TreeItem[String] = _
  var clientTabPaneController: RedisClientTabPaneController = _

  {
    this.setText(redisKey)
    this.setContent(controller.getParent)
  }

  def deleteKey(): Future[Boolean] = Future {
    val client = RedisFxPaneController.getRedisClient(uuid)
    val updated = Await.result[Long](client.del(redisKey, index), Duration.Inf)
    if (updated > 0) Platform.runLater(() => {
      this.getTabPane.getTabs.remove(this)
      this.clientTabPaneController.delTreeItem(treeItem)
    })
    updated > 0
  }
}
