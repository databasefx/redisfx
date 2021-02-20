package cn.navigational.redisfx.controls

import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controller.pane.{RedisClientTabPaneController, RedisValTabController}
import cn.navigational.redisfx.util.AsyncUtil
import javafx.application.Platform
import javafx.scene.control.{Tab, TreeItem}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


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
    val updated = AsyncUtil.awaitWithInf(client.del(redisKey, index))
    if (updated > 0) Platform.runLater(() => {
      this.getTabPane.getTabs.remove(this)
      this.clientTabPaneController.delTreeItem(treeItem)
    })
    updated > 0
  }
}
