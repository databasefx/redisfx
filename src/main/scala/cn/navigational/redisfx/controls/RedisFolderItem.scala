package cn.navigational.redisfx.controls

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controls.RedisFolderItem.FOLDER_ICON
import cn.navigational.redisfx.model.RedisKey
import cn.navigational.redisfx.util.{AsyncUtil, RedisDataUtil}
import javafx.scene.image.{Image, ImageView}

import scala.concurrent.duration.Duration

object RedisFolderItem {
  private val FOLDER_ICON = new Image(RedisFxResource.load("icon/folder.png").openStream())
}

class RedisFolderItem(val redisKey: RedisKey) extends RedisFxTreeItem {
  {
    this.setValue(redisKey.key)
    this.setGraphic(new ImageView(FOLDER_ICON))
  }

  override def refreshEvent(): Unit = {
    val qk = s"${getRowKey}${RedisDataUtil.FILE_SEPARATOR}*"
    this._refresh[Unit](() => {
      val uuid = redisKey.uuid
      val client = RedisFxPaneController.getRedisClient(uuid)
      val arr = AsyncUtil.awaitWithInf(client.lsKey(qk, redisKey.index))
        .map(it => it.substring(qk.length - 1))
      this.createChildNode(RedisDataUtil.getRedisKeyTreeData(arr, redisKey.index, uuid))
    })
  }
}
