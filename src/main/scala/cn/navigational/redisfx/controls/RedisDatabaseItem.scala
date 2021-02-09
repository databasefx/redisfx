package cn.navigational.redisfx.controls

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controls.RedisDatabaseItemConstant.DB_ICON
import cn.navigational.redisfx.model.RedisKey
import cn.navigational.redisfx.util.RedisDataUtil
import javafx.application.Platform
import javafx.scene.image.{Image, ImageView}

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object RedisDatabaseItemConstant {
  val DB_ICON = new Image(RedisFxResource.load("icon/db.png").openStream())
}

class RedisDatabaseItem(val index: Int, val uuid: String) extends RedisFxTreeItem {

  {
    this.setValue(s"db$index")
    this.setGraphic(new ImageView(DB_ICON))
  }

  /**
   * 响应刷新事件
   */
  override def refreshEvent(): Unit = {
    val future = this._refresh[Array[RedisKey]](() => {
      val keys = Await.result[Array[String]](RedisFxPaneController.getRedisClient(uuid).lsAllKey(index), Duration.Inf)
      RedisDataUtil.getRedisKeyTreeData(keys, index)
    })
    future onComplete {
      case Success(arr) =>
        Platform.runLater(() => {
          getChildren.clear()
          arr.foreach(it => getChildren.add(new RedisKeyTreeItem(it)))
        })
      case Failure(ex) =>
    }
  }
}
