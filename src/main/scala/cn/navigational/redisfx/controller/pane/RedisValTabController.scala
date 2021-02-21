package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.{AbstractFXMLController, AbstractRedisContentService}
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.{AddRedisKeyController, RedisFxPaneController}
import cn.navigational.redisfx.controls.RedisValTab
import cn.navigational.redisfx.enums.RedisDataType
import cn.navigational.redisfx.helper.NotificationHelper
import cn.navigational.redisfx.model.{AddRedisKeyMetaModel, RedisRichValueModel}
import cn.navigational.redisfx.util.AsyncUtil
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.{Label, TextField}
import javafx.scene.layout.BorderPane

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class RedisValTabController(val valTab: RedisValTab) extends AbstractFXMLController[BorderPane](RedisFxResource.load("fxml/pane/RedisValTabPane.fxml")) {
  @FXML
  private var tLabel: Label = _
  @FXML
  private var keyTextF: TextField = _
  /**
   * 当前key TTL值
   */
  private var ttl: Long = -1
  /**
   * 临时变量用于判断当前redis key数据结构是否能发生改变
   */
  private var currentDataType: RedisDataType = _

  private var contentPaneController: AbstractRedisContentService[_ <: Node] = _

  {
    initVal()
    this.keyTextF.setText(valTab.redisKey)
    this.valTab.setOnCloseRequest(_ => this.onCloseRequest())
  }

  @FXML
  private def renameKey(): Unit = {

  }

  @FXML
  private def deleteKey(): Unit = {
    val confirm = NotificationHelper.showConfirmAlert(msg = s"你确定要删除${valTab.redisKey}?")
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
        this.tLabel.setText(s"TTL:${ttl.toString}")
        promise.success(value)
      })
      case Failure(ex) => promise.failure(ex)
    })
  }

  @FXML
  def reloadVal(): Unit = {
    this.initVal()
  }

  def initVal(): Future[Unit] = Future {
    val promise = showLoad[Unit]()
    try {
      val client = RedisFxPaneController.getRedisClient(valTab.uuid)
      val exists = AsyncUtil.awaitWithInf(client.exists(valTab.redisKey, valTab.index))
      if (exists) {
        this.ttl = AsyncUtil.awaitWithInf(client.ttl(valTab.redisKey, valTab.index))
        val dataType = AsyncUtil.awaitWithInf(client.typeKey(valTab.redisKey, valTab.index))
        val updated = currentDataType == null || currentDataType != dataType
        if (updated) {
          this.onCloseRequest()
          this.contentPaneController = if (dataType == RedisDataType.STRING) {
            new StringContentPaneController(this)
          } else {
            new RichTextFormContentPaneController(this)
          }
          this.currentDataType = dataType
        }
        AsyncUtil.awaitWithInf(this.contentPaneController.onContentUpdate(client, valTab.redisKey, valTab.index, dataType, updated))
        Platform.runLater(() => {
          this.tLabel.setText(s"TTL:$ttl")
          this.innerPane.setCenter(this.contentPaneController.getParent)
        })
        promise.success()
      } else {
        valTab.deleteKey(false)
      }
    } catch {
      case ex: Exception => promise.failure(ex)
    }
  }

  def addRichTextRow(): Unit = {
    val meta = new AddRedisKeyMetaModel(ttl, valTab.redisKey, valTab.index, currentDataType)
    val controller = new AddRedisKeyController(valTab.uuid, meta, () => this.initVal())
    controller.openWindow(true)
  }

  def deleteRichRow(member: RedisRichValueModel): Future[Long] = Future {
    val index = valTab.index
    val redisKey = valTab.redisKey
    val client = RedisFxPaneController.getRedisClient(valTab.uuid)
    AsyncUtil.awaitWithInf(this.currentDataType match {
      case RedisDataType.SET => client.sRem(redisKey, index, member.getValue)
      case RedisDataType.Z_SET => client.zRem(redisKey, index, member.getValue)
      case RedisDataType.HASH => client.hDel(valTab.redisKey, index, member.getKey)
      case RedisDataType.LIST => client.lRem(redisKey, 1, index, member.getValue)
    })
  }

  /**
   * 释放每个RedisContentPaneController占用资源
   */
  private def onCloseRequest(): Unit = {
    if (this.contentPaneController == null) {
      return
    }
    this.contentPaneController.contentPaneRequestClose()
  }

}
