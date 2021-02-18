package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractRedisContentService
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.enums.{RedisDataType, RedisDataViewFormat}
import cn.navigational.redisfx.util.{JedisUtil, RedisDataUtil}
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.scene.control.{ChoiceBox, Label, TextArea}
import javafx.scene.layout.BorderPane

import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.charset.Charset
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class StringContentPaneController(valTabController: RedisValTabController) extends AbstractRedisContentService[BorderPane](valTabController, RedisFxResource.load("fxml/pane/StringContentPane.fxml")) {
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
   * 执行更新操作
   *
   * @param client jedis工具类
   * @param redisKey  key
   * @param index     数据库指数
   * @param dataType  redis数据类型
   * @return
   */
  override def onContentUpdate(client: JedisUtil, redisKey: String, index: Int, dataType: RedisDataType): Future[Unit] = Future {
    val data = Await.result[String](client.get(redisKey, index), Duration.Inf)
    val size = data.getBytes(Charset.forName("UTF8")).length
    //如果未指定数据格式=>自动判断
    if (viewFormat == null) {
      viewFormat = RedisDataUtil.getRedisDataViewFormat(data)
    }
    val formatVal = RedisDataUtil.formatViewData(data, viewFormat)
    Platform.runLater(() => {
      this.textArea.setText(formatVal)
      this.dataSize.setText(s"$size 字节")
      this.dataFormat.setText(RedisDataType.STRING.getName)
      this.dataViewFormat.getSelectionModel.select(viewFormat.getName)
    })
  }

  override def contentPaneRequestClose(): Unit = {
    this.dataViewFormat.getSelectionModel.selectedItemProperty().removeListener(this.viewDataTypeListener)
  }

}
