package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractRedisContentService
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.AddRedisKeyController
import cn.navigational.redisfx.enums.{RedisDataType, RichTextTableColumn}
import cn.navigational.redisfx.model.RedisRichValueModel
import cn.navigational.redisfx.util.{AsyncUtil, JedisUtil}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.{TableColumn, TableView, TextField}
import javafx.scene.layout.{BorderPane, HBox}

import java.util
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.{BufferHasAsJava, CollectionHasAsScala}
import scala.util.{Failure, Success}

class RichTextFormContentPaneController(valTabController: RedisValTabController) extends AbstractRedisContentService[BorderPane](valTabController, RedisFxResource.load("fxml/pane/RichTextFormContentPane.fxml")) {
  @FXML
  private var pageContainer: HBox = _
  @FXML
  private var pageIndicator: TextField = _
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
    this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY)
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
  override def onContentUpdate(client: JedisUtil, redisKey: String, index: Int, dataType: RedisDataType, update: Boolean): Future[Unit] = Future {
    if (update) {
      this.createTableColumn(dataType)
    }
    val offset = pageIndex * pageSize
    val end = offset + pageSize
    val list = dataType match {
      case RedisDataType.HASH =>
        val map = AsyncUtil.awaitWithInf(client.hGet(redisKey, index))
        val arr = new ArrayBuffer[String]()
        map.keySet().forEach(arr.addOne)
        map.values().forEach(arr.addOne)
        arr.toArray
      case RedisDataType.LIST =>
        AsyncUtil.awaitWithInf(client.lRange(redisKey, offset, end, index))
      case RedisDataType.SET =>
        AsyncUtil.awaitWithInf(client.sMember(redisKey, index))
      case RedisDataType.Z_SET =>
        AsyncUtil.awaitWithInf(client.zRange(redisKey, offset, end, index))
    }
    val arr = new ArrayBuffer[RedisRichValueModel]()
    if (dataType == RedisDataType.HASH) {
      val len = list.length
      val temp = len / 2
      for (i <- 0 until (temp)) {
        val model = new RedisRichValueModel()
        model.setIndex(i + 1)
        model.setKey(list(i))
        model.setValue(list(i + temp))
        arr.addOne(model)
      }
    } else {
      var i = pageIndex * pageSize + 1
      for (elem <- list) {
        val model = new RedisRichValueModel()
        if (dataType == RedisDataType.Z_SET) {
          val score = AsyncUtil.awaitWithInf(client.zScore(redisKey, elem, index))
          model.setScore(score)
        }
        model.setIndex(i)
        model.setValue(elem)
        arr.addOne(model)
        i += 1
      }
    }
    Platform.runLater(() => {
      this.tableView.getItems.clear()
      this.tableView.getItems.addAll(arr.asJava)
      //动态判断当前数据结构是否可以分页
      this.pageContainer.setVisible(dataType.isPaging)
      this.pageIndicator.setText((this.pageIndex + 1).toString)
    })
  }

  /**
   * 动态创建表列
   *
   * @param dataType redis 数据类型
   */
  private def createTableColumn(dataType: RedisDataType): Unit = {
    val ts = RichTextTableColumn.getTableColumn(dataType).asScala.toArray
    val columns: java.util.List[TableColumn[RedisRichValueModel, Object]] = new util.ArrayList()
    for (elem <- ts) {
      val column: TableColumn[RedisRichValueModel, Object] = new TableColumn(elem.getColumnName)
      column.setCellValueFactory(new PropertyValueFactory(elem.getMapperFiled))
      column.setSortable(false)
      columns.add(column)
    }
    Platform.runLater(() => {
      this.tableView.getColumns.clear()
      this.tableView.getColumns.addAll(columns)
    })
  }

  @FXML
  def next(): Unit = {
    pageIndex += 1
    //如果失败将页数返回减数前
    valTabController.initVal() onComplete {
      case _ =>
      case Failure(ex) => pageIndex -= 1
    }
  }

  @FXML
  def previous(): Unit = {
    if (pageIndex <= 0) {
      return
    }
    pageIndex -= 1
    //如果失败将页数返回减数前
    valTabController.initVal() onComplete {
      case _ =>
      case Failure(ex) => pageIndex += 1
    }
  }

  @FXML
  def addRow(): Unit = {
    valTabController.addRichTextRow()
  }

  @FXML
  def deleteRow(): Unit = {
    val selectItem = this.tableView.getSelectionModel.getSelectedItem
    if (selectItem == null) {
      return
    }
    this.showLoad("删除中...", errTitle = "删除失败", func = () => {
      val value = AsyncUtil.awaitWithInf(this.valTabController.deleteRichRow(selectItem))
      if (value > 0) {
        this.valTabController.initVal()
      }
    })

  }
}

