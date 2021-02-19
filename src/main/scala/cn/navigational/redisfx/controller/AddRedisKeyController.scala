package cn.navigational.redisfx.controller

import cn.navigational.redisfx.AbstractViewController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.pane.RedisClientTabPaneController
import cn.navigational.redisfx.enums.RedisDataType
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, TextArea, TextField}
import javafx.scene.layout.{GridPane, VBox}
import javafx.stage.WindowEvent

import java.util
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 *
 *
 *
 * @author yangkui
 * @since 1.0
 */
class AddRedisKeyController(private val ownerController: RedisClientTabPaneController) extends AbstractViewController[GridPane]("新建KEY", RedisFxResource.load("fxml/AddRedisKeyView.fxml")) {
  @FXML
  private var contentBox: VBox = _
  @FXML
  private var keyField: TextField = _
  @FXML
  private var ttlField: TextField = _
  @FXML
  private var scoreField: TextField = _
  @FXML
  private var regularField: TextArea = _
  @FXML
  private var dbBox: ChoiceBox[String] = _
  @FXML
  private var typeChoiceBox: ChoiceBox[String] = _

  private val hashValue: TextArea = new TextArea()
  private val selectListener: ChangeListener[String] = (_, _, newVal) => {
    val dataType = RedisDataType.getDataType(newVal)
    this.scoreField.setDisable(true)
    this.ttlField.setDisable(!dataType.isTtl)
    dataType match {
      case RedisDataType.HASH =>
        if (contentBox.getChildren.size() <= 1) {
          this.contentBox.getChildren.add(hashValue)
        }
        this.ttlField.setDisable(false)
      case RedisDataType.Z_SET =>
        this.scoreField.setDisable(false)
      case _ =>
        this.ttlField.setDisable(false)
        this.contentBox.getChildren.remove(hashValue)
    }
  }

  {
    this.initDatabase()
    for (elem <- RedisDataType.values()) {
      this.typeChoiceBox.getItems.add(elem.getName)
    }
    //初始化选中字符串
    this.typeChoiceBox.getSelectionModel.select(RedisDataType.STRING.getName)
    this.typeChoiceBox.getSelectionModel.selectedItemProperty().addListener(this.selectListener)
    this.getStage.showAndWait()
  }

  override def onWindowRequestClose(event: WindowEvent): Unit = {
    this.typeChoiceBox.getSelectionModel.selectedItemProperty().removeListener(this.selectListener)
  }

  @FXML
  def createRedisKey(): Unit = {
    val str = this.typeChoiceBox.getSelectionModel.getSelectedItem
    val dataType = RedisDataType.getDataType(str)
    val keyVal = this.keyField.getText()
    val value = this.regularField.getText()
    val database = this.dbBox.getSelectionModel.getSelectedIndex
    val client = RedisFxPaneController.getRedisClient(ownerController.uuid)
    val promise = this.showLoad[Boolean]("设置中...")
    val future = dataType match {
      case RedisDataType.HASH =>
        val hValue = hashValue.getText()
        val attr = new util.HashMap[String, String]() {
          this.put(value, hValue)
        }
        client.hSet(keyVal, attr, database)
      case RedisDataType.LIST => client.lPush(keyVal, value, database)
      case RedisDataType.SET => client.sAdd(keyVal, value, database)
      case RedisDataType.Z_SET =>
        val score = this.scoreField.getText.toDouble
        client.zAdd(keyVal, value, score, database)
      case _ =>
        val ttl = this.ttlField.getText().toInt
        client.setEx(keyVal, value, database, ttl)
    }
    future onComplete {
      case Success(value) => promise.success(value)
        this.close()
      case Failure(ex) => promise.failure(ex)
    }
  }

  private def initDatabase(): Unit = {
    val client = RedisFxPaneController.getRedisClient(ownerController.uuid)
    val future = client.listDbCount()
    val promise = this.showLoad[Int]("初始化数据库中..")
    future onComplete {
      case Success(value) =>
        promise.success(value)
        val arr = new util.ArrayList[String](value)
        for (index <- 0 until value) {
          arr.add(s"数据库:$index")
        }
        Platform.runLater(() => {
          this.dbBox.getItems.addAll(arr)
          this.dbBox.getSelectionModel.select(0)
        })
      case Failure(ex) => promise.failure(ex)
    }
  }
}
