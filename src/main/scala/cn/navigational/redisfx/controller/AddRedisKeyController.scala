package cn.navigational.redisfx.controller

import cn.navigational.redisfx.AbstractViewController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.pane.RedisClientTabPaneController
import cn.navigational.redisfx.enums.RedisDataType
import cn.navigational.redisfx.helper.NotificationHelper
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.ChoiceBox
import javafx.scene.layout.GridPane

/**
 *
 *
 *
 * @author yangkui
 * @since 1.0
 */
class AddRedisKeyController(private val ownerController: RedisClientTabPaneController) extends AbstractViewController[GridPane]("新建KEY", RedisFxResource.load("fxml/AddRedisKeyView.fxml")) {
  @FXML
  private var typeChoiceBox: ChoiceBox[String] = _
  private var redisDataType: RedisDataType = _

  {
    for (elem <- RedisDataType.values()) {
      this.typeChoiceBox.getItems.add(elem.getName)
    }
    //初始化选中字符串
    this.typeChoiceBox.getSelectionModel.select(RedisDataType.STRING.getName)
    this.getStage.showAndWait()
  }

  @FXML
  def createRedisKey(): Unit = {
    val str = this.typeChoiceBox.getSelectionModel.getSelectedItem
    val dataType = RedisDataType.getDataType(str)
    if (dataType != RedisDataType.STRING) {
      NotificationHelper.showInfo("暂不支持该数据类型", Pos.TOP_CENTER)
      return
    }

  }
}
