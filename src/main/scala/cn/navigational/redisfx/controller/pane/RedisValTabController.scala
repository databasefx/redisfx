package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractFXMLController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controls.RedisValTab
import cn.navigational.redisfx.enums.{RedisDataType, RedisDataViewFormat}
import cn.navigational.redisfx.helper.NotificationHelper
import cn.navigational.redisfx.util.{JSONUtil, RedisDataUtil}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{Label, TextField}
import javafx.scene.layout.BorderPane

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class RedisValTabController(private val valTab: RedisValTab) extends AbstractFXMLController[BorderPane](RedisFxResource.load("fxml/pane/RedisValTabPane.fxml")) {
  @FXML
  private var tLabel: Label = _
  @FXML
  private var keyTextF: TextField = _

  private val contentPaneController = new StringContentPaneController() {
    this.valTabController = RedisValTabController.this
  }

  {
    initVal()
    this.parent.setCenter(contentPaneController.getParent)
  }

  @FXML
  private def renameKey(): Unit = {

  }

  @FXML
  private def deleteKey(): Unit = {
    val confirm = NotificationHelper.showConfirmAlert(msg = "你确定要删除?")
    if (!confirm) {
      return
    }
    val promise = showLoad[Boolean]("删除中...", showError = false)
    val future = valTab.deleteKey()
    future.onComplete(ar => {
      if (ar.isFailure) {
        showError("删除失败", ar.failed.get)
      } else {
        promise.success(ar.get)
      }
    })
  }

  @FXML
  private def updateTTL(): Unit = {
    val optional = NotificationHelper.showInputDialog("请输入新的TTL值")
    if (optional.isEmpty || optional.get().trim == "") {
      return
    }
    val ttl = optional.get().toInt
    val promise = showLoad[Long]("更新TTL中...", "更新TTL出错")
    val client = RedisFxPaneController.getRedisClient(valTab.uuid)
    val future = client.setTtl(valTab.redisKey, ttl, valTab.index)
    future.onComplete({
      case Success(value) => Platform.runLater(() => {
        this.tLabel.setText(ttl.toString)
        promise.success(value)
      })
      case Failure(ex) => promise.failure(ex)
    })
  }

  @FXML
  def reloadVal(): Unit = {
    this.initVal()
  }

  /**
   * 加载value值
   *
   */
  def initVal(): Future[Unit] = Future {
    val promise = showLoad[Unit]()
    try {
      val client = RedisFxPaneController.getRedisClient(valTab.uuid)
      val ttl = Await.result[Long](client.ttl(valTab.redisKey, valTab.index), Duration.Inf)
      val dataType = Await.result[RedisDataType](client.typeKey(valTab.redisKey, valTab.index), Duration.Inf)
      val text = dataType match {
        case RedisDataType.HASH => Await.result[String](client.hGet(valTab.redisKey, valTab.index), Duration.Inf)
        case RedisDataType.LIST =>
          val len = Await.result[Long](client.lLen(valTab.redisKey, valTab.index), Duration.Inf)
          val list = Await.result[Array[String]](client.lRange(valTab.redisKey, 0, len, valTab.index), Duration.Inf)
          RedisDataUtil.formatListVal(list)
        case RedisDataType.SET =>
          val arr = Await.result[Array[String]](client.sMember(valTab.redisKey, valTab.index), Duration.Inf)
          JSONUtil.objToJsonStr(arr)
        case RedisDataType.Z_SET =>
          val len = Await.result[Long](client.zCard(valTab.redisKey, valTab.index), Duration.Inf)
          val arr = Await.result[Array[String]](client.zRange(valTab.redisKey, 0, len, valTab.index), Duration.Inf)
          RedisDataUtil.formatListVal(arr)
        case _ => Await.result[String](client.get(valTab.redisKey, valTab.index), Duration.Inf)
      }
      Platform.runLater(() => {
        this.tLabel.setText(s"TTL:$ttl")
        this.keyTextF.setText(valTab.redisKey)
        contentPaneController.contentUpdate(text, dataType)
      })
      promise.success()
    } catch {
      case ex: Exception => promise.failure(ex)
    }
  }

}
