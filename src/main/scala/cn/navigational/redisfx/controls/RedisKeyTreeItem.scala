package cn.navigational.redisfx.controls

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controls.RedisKeyTreeItem.{FOLDER_ICON, KEY_ICON}
import cn.navigational.redisfx.helper.{JedisHelper, NotificationHelper}
import cn.navigational.redisfx.model.RedisKey
import javafx.scene.image.{Image, ImageView}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}


object RedisKeyTreeItem {
  private val KEY_ICON = new Image(RedisFxResource.load("icon/key.png").openStream())
  private val FOLDER_ICON = new Image(RedisFxResource.load("icon/folder.png").openStream())
}

class RedisKeyTreeItem(private val redisKey: RedisKey) extends RedisFxTreeItem {

  {
    val graphic = new ImageView({
      if (redisKey.isLeaf) {
        KEY_ICON
      } else {
        FOLDER_ICON
      }
    })
    this.setValue(redisKey.key)
    this.setGraphic(graphic)
    for (elem <- redisKey.sub) {
      this.getChildren.add(new RedisKeyTreeItem(elem))
    }
  }

  def getRowKey: String = {
    redisKey.rowKey
  }


  /**
   * 响应删除事件
   */
  override def deleteEvent(uuid: String): Future[Boolean] = {
    //排除文件夹响应事件
    if (!canDelete) {
      return Future.successful(false)
    }
    val confirm = NotificationHelper.showConfirmAlert(msg = s"你确定要删除${getRowKey}?")
    if (!confirm) {
      return Future.successful(false)
    }
    val promise = Promise[Boolean]()
    val client = RedisFxPaneController.getRedisClient(uuid)
    val future = client.del(getRowKey, index)
    future onComplete {
      case Success(value) => promise.success(value > 0)
      case Failure(ex) =>
        ex.printStackTrace()
        promise.success(false)
    }
    promise.future
  }

  /**
   * 判断当前Redis Key是否能删除
   *
   * @return
   */
  def canDelete: Boolean = {
    redisKey.isLeaf
  }

  def index: Int = this.redisKey.index
}
