package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractRedisContentService
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.enums.RedisDataType
import cn.navigational.redisfx.model.RedisRichValueModel
import cn.navigational.redisfx.util.JedisUtil
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.BorderPane

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.BufferHasAsJava

class FormContentPaneController(valTabController: RedisValTabController) extends AbstractRedisContentService[BorderPane](valTabController, RedisFxResource.load("fxml/pane/FormContentPane.fxml")) {
  @FXML
  private var tableView: TableView[RedisRichValueModel] = _

  /**
   * 分页查询页数
   */
  private var pageIndex: Int = 0
  /**
   * 分页查询偏移量
   */
  private val pageSize: Int = 10

  {
    val columns = tableView.getColumns
    columns.get(0).setCellValueFactory(new PropertyValueFactory("index"))
    columns.get(1).setCellValueFactory(new PropertyValueFactory("value"))
  }

  /**
   * 执行更新操作
   *
   * @param client   jedis工具类
   * @param redisKey key
   * @param index    数据库指数
   * @param dataType redis数据类型
   * @return
   */
  override def onContentUpdate(client: JedisUtil, redisKey: String, index: Int, dataType: RedisDataType): Future[Unit] = Future {
    val offset = pageIndex * pageSize + pageSize
    val list = dataType match {
      case RedisDataType.HASH =>
        val map = Await.result[java.util.Map[String, String]](client.hGet(redisKey, index), Duration.Inf)
        val arr = new ArrayBuffer[String]()
        map.keySet().forEach(arr.addOne)
        map.values().forEach(arr.addOne)
        arr.toArray
      case RedisDataType.LIST =>
        val len = Await.result[Long](client.lLen(redisKey, index), Duration.Inf)
        Await.result[Array[String]](client.lRange(redisKey, pageIndex, offset, index), Duration.Inf)
      case RedisDataType.SET =>
        Await.result[Array[String]](client.sMember(redisKey, index), Duration.Inf)
      case RedisDataType.Z_SET =>
        val len = Await.result[Long](client.zCard(redisKey, index), Duration.Inf)
        Await.result[Array[String]](client.zRange(redisKey, pageIndex, offset, index), Duration.Inf)
    }
    val arr = new ArrayBuffer[RedisRichValueModel]()
    if (dataType == RedisDataType.HASH) {
      val len = list.length
      val temp = len / 2
      for (i <- 0 until (temp)) {
        val model = new RedisRichValueModel()
        model.setIndex(list(i))
        model.setValue(list(i + temp))
        arr.addOne(model)
      }
    } else {
      var index = pageIndex * pageSize+1
      for (elem <- list) {
        val model = new RedisRichValueModel()
        model.setIndex(index.toString)
        model.setValue(elem)
        arr.addOne(model)
        index += 1
      }
    }
    Platform.runLater(() => {
      this.tableView.getItems.clear()
      this.tableView.getItems.addAll(arr.asJava)
    })
  }
}

