package cn.navigational.redisfx.controls

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controls.RedisKeyTreeItem.KEY_ICON
import cn.navigational.redisfx.helper.NotificationHelper
import cn.navigational.redisfx.model.RedisKey
import javafx.scene.image.{Image, ImageView}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


object RedisKeyTreeItem {
  private val KEY_ICON = new Image(RedisFxResource.load("icon/key.png").openStream())
}

class RedisKeyTreeItem(private val redisKey: RedisKey) extends RedisFxTreeItem {

  {
    this.setValue(redisKey.key)
    this.setGraphic(new ImageView(KEY_ICON))
  }


  /**
   * 响应删除事件
   */
  override def deleteEvent(uuid: String): Future[Boolean] = {
    val confirm = NotificationHelper.showConfirmAlert(msg = s"你确定要删除${getRowKey}?")
    if (!confirm) {
      return Future.successful(false)
    }
    this._refresh[Boolean](() => {
      val client = RedisFxPaneController.getRedisClient(uuid)
      Await.result[Long](client.del(getRowKey, index), Duration.Inf) > 0
    })
  }

  def index: Int = this.redisKey.index
}
