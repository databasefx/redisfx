package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractRedisContentService
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.enums.{RedisDataType, RedisDataViewFormat}
import cn.navigational.redisfx.util.RedisDataUtil
import javafx.beans.value.ChangeListener
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, Label, TextArea}
import javafx.scene.layout.BorderPane

import java.nio.charset.Charset

class StringContentPaneController extends AbstractRedisContentService[BorderPane, String](RedisFxResource.load("fxml/pane/StringContentPane.fxml")) {
  @FXML
  private var dataSize: Label = _
  @FXML
  private var dataFormat: Label = _
  @FXML
  private var textArea: TextArea = _

  @FXML
  private var dataViewFormat: ChoiceBox[String] = _
  private var viewFormat: RedisDataViewFormat = _
  private val viewDataTypeListener: ChangeListener[String] = (_, _, newVal) => {
    this.viewFormat = RedisDataViewFormat.getViewFormat(newVal)
    this.valTabController.initVal()
  }

  {
    for (item <- RedisDataViewFormat.values()) {
      dataViewFormat.getItems.add(item.getName)
    }
    dataViewFormat.getSelectionModel.select(RedisDataViewFormat.PLAINT_TEXT.getName)
    dataViewFormat.getSelectionModel.selectedItemProperty().addListener(this.viewDataTypeListener)
  }


  /**
   * 内容发生改变时调用
   *
   * @param data 外部传入数据
   */
  override def contentUpdate(data: String, dataType: RedisDataType): Unit = {
    val size = data.getBytes(Charset.forName("UTF8")).length
    //如果未指定数据格式=>自动判断
    if (viewFormat == null) {
      viewFormat = RedisDataUtil.getRedisDataViewFormat(data)
    }
    val formatVal = RedisDataUtil.formatViewData(data, viewFormat)
    this.textArea.setText(formatVal)
    this.dataSize.setText(s"$size 字节")
    this.dataFormat.setText(dataType.getName)
    this.dataViewFormat.getSelectionModel.select(viewFormat.getName)
  }

  override def contentPaneRequestClose(event: Event): Unit = {
    this.dataViewFormat.getSelectionModel.selectedItemProperty().removeListener(this.viewDataTypeListener)
  }

}
