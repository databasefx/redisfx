package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractFXMLController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controls.RedisValTab
import cn.navigational.redisfx.helper.NotificationHelper
import cn.navigational.redisfx.util.{RedisDataType, RedisDataUtil}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, Label, TextArea, TextField}
import javafx.scene.layout.BorderPane

import java.nio.charset.Charset
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class RedisValTabController(private val valTab: RedisValTab) extends AbstractFXMLController[BorderPane](RedisFxResource.load("fxml/pane/RedisValTabPane.fxml")) {
  @FXML
  private var tLabel: Label = _
  @FXML
  private var textArea: TextArea = _
  @FXML
  private var dataSize: Label = _
  @FXML
  private var keyTextF: TextField = _
  @FXML
  private var dataFormat: ChoiceBox[String] = _

  private var value: String = _


  {
    initVal()
    dataFormat.getItems.addAll("TEXT", "JSON", "XML")
    dataFormat.getSelectionModel.selectedItemProperty().addListener((_, _, item) => {
      val dataType = RedisDataType.getDataType(item)
      val format = RedisDataUtil.formatVal(value, dataType)
      this.textArea.setText(format)
    })
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

  private def updateText(text: String, ttl: Long): Unit = {
    this.value = text
    val size = text.getBytes(Charset.forName("UTF8")).length
    Platform.runLater(() => {
      this.textArea.setText(text)
      this.dataSize.setText(s"$size 字节")
      this.tLabel.setText(s"TTL:$ttl")
      this.keyTextF.setText(valTab.redisKey)
      dataFormat.getSelectionModel.select(0)
    })
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
      val text = Await.result[String](client.get(valTab.redisKey, valTab.index), Duration.Inf)
      this.updateText(text, ttl)
      promise.success()
    } catch {
      case ex: Exception => promise.failure(ex)
    }
  }
}
