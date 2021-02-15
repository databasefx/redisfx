package cn.navigational.redisfx.controller

import cn.navigational.redisfx.{AbstractDialogFXMLDialog, AbstractViewController}
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.helper.{AssetHelper, JedisHelper, NotificationHelper}
import cn.navigational.redisfx.io.RedisFxIO
import cn.navigational.redisfx.model.RedisConnectInfo
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.{PasswordField, TextField}
import javafx.scene.layout.GridPane
import org.controlsfx.control.ToggleSwitch

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class RedisConnectionController extends AbstractViewController[GridPane]("新建连接", RedisFxResource.load("fxml/RedisConnectView.fxml")) {
  @FXML
  private var name: TextField = _
  @FXML
  private var host: TextField = _
  @FXML
  private var port: TextField = _
  @FXML
  private var password: PasswordField = _
  @FXML
  private var saveLocal: ToggleSwitch = _

  {
    this.getStage.showAndWait()
  }

  @FXML
  def toSave(): Unit = {
    val hVal = this.host.getText
    val name = this.name.getText
    val pw = this.password.getText
    val pVal = this.port.getText().toInt
    val localSave = this.saveLocal.isSelected

    val info = new RedisConnectInfo()
    info.name = name
    info.host = hVal
    info.password = pw
    info.port = pVal.toInt
    info.localSave = localSave
    info.uuid = UUID.randomUUID().toString

    //持久化到本地
    if (localSave) {
      RedisFxIO.saveConnectInfo(info)
    }
    RedisMainViewController.redisConnectList.add(info)
    this.close()
  }

  @FXML
  def pingRedis(): Unit = {
    val config = new RedisConnectInfo()
    config.password = this.password.getText
    config.host = this.host.getText
    config.port = this.port.getText.toInt
    val promise = this.showLoad[String]("连接中...", errTitle = "连接失败")
    JedisHelper.pingRedis(config) onComplete {
      case Success(value) =>
        promise.success(value)
        NotificationHelper.showInfo("连接成功", Pos.TOP_RIGHT)
      case Failure(ex) => promise.failure(ex)
    }
  }
}
