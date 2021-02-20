package cn.navigational.redisfx.controller

import cn.navigational.redisfx.AbstractViewController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisMainViewController.redisConnectList
import cn.navigational.redisfx.enums.{MainTableColumn, TableMenAction}
import cn.navigational.redisfx.helper.{JedisHelper, NotificationHelper}
import cn.navigational.redisfx.io.RedisFxIO
import cn.navigational.redisfx.model.RedisConnectInfo
import cn.navigational.redisfx.util.DateUtil
import javafx.application.Platform
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, MenuItem, TableColumn, TableView}
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{AnchorPane, BorderPane}
import javafx.stage.{Stage, StageStyle, WindowEvent}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.CollectionHasAsScala

object RedisMainViewController {
  val redisConnectList: ObservableList[RedisConnectInfo] = FXCollections.observableArrayList()
}

class RedisMainViewController extends AbstractViewController[BorderPane]("RedisFX Desktop Client", RedisFxResource.load("fxml/RedisMainView.fxml")) {
  @FXML
  private var sysBar: AnchorPane = _
  @FXML
  private var tableView: TableView[RedisConnectInfo] = _

  {
    this.tableView.setSortPolicy(_ => null)
    new StageDragListener(getStage, sysBar)
  }

  private val perRedisConfigListener: ListChangeListener[RedisConnectInfo] = (c: ListChangeListener.Change[_ <: RedisConnectInfo]) => {
    while (c.next()) {
      if (c.wasAdded()) {
        val list = c.getAddedSubList
        tableView.getItems.addAll(list)
      }
    }
  }

  {
    MainTableColumn.values().foreach(it => {
      val tableColumn = new TableColumn[RedisConnectInfo, Object](it.getName)
      tableColumn.setCellValueFactory(new PropertyValueFactory(it.getMapper))
      tableView.getColumns.add(tableColumn)
    })
    val cMenu = new ContextMenu()
    TableMenAction.values().foreach(it => {
      val item = new MenuItem(it.getName) {
        this.setUserData(it)
        this.setOnAction(tableViewMenuAction)
      }
      cMenu.getItems.add(item)
    })
    this.tableView.setContextMenu(cMenu)
    this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY)
    RedisMainViewController.redisConnectList.addListener(this.perRedisConfigListener)
    this.loadPerConfigFromDisk()
    this.getStage.initStyle(StageStyle.UNDECORATED)
    this.getStage.show()
  }

  @FXML
  def exit(event: ActionEvent): Unit = {
    Platform.exit()
  }

  @FXML
  def openAbout(event: ActionEvent): Unit = {
    new RedisFxAboutController()
  }

  @FXML
  def miniWin(event: ActionEvent): Unit = {
    this.getStage.setIconified(true)
  }

  @FXML
  def createNewRedisClient(event: ActionEvent): Unit = {
    new RedisConnectionController()
  }

  /**
   * 处理TableView菜单事件
   *
   * @param event 事件源
   */
  private def tableViewMenuAction(event: ActionEvent): Unit = {
    val selectItem = tableView.getSelectionModel.getSelectedItem
    if (selectItem == null) {
      return
    }
    val items = this.tableView.getItems
    val item = event.getSource.asInstanceOf[MenuItem]
    val action = item.getUserData
    action match {
      case TableMenAction.OPEN =>
        //更新最后使用时间
        selectItem.setLastUseDate(DateUtil.formatNow())
        RedisFxIO.saveConnectFile(items.asScala.toArray)
        this.tableView.refresh()
        RedisFxPaneController.addRedisClient(selectItem)
      case TableMenAction.DELETE =>
        val result = NotificationHelper.showConfirmAlert(msg = "你确定要删除该连接?")
        if (!result) {
          return
        }
        items.remove(selectItem)
        RedisFxIO.saveConnectFile(items.asScala.toArray)
    }
  }

  private def loadPerConfigFromDisk(): Unit = {
    val arr = Await.result(RedisFxIO.getConnectFile, Duration.Inf)
    for (element <- arr) {
      redisConnectList.add(element)
    }
  }

  override def onWindowRequestClose(event: WindowEvent): Unit = {
    RedisMainViewController.redisConnectList.removeListener(this.perRedisConfigListener)
  }
}

import javafx.event.EventHandler

/**
 *
 * 自定义窗口行为(窗口拖动)
 *
 * @author yangkui
 *
 */
class StageDragListener(val stage: Stage, val target: Node) extends EventHandler[MouseEvent] {
  private var xOffset: Double = 0
  private var yOffset: Double = 0


  {
    target.setOnMousePressed(this)
    target.setOnMouseDragged(this)
  }

  override def handle(event: MouseEvent): Unit = {
    event.consume()
    if (event.getEventType eq MouseEvent.MOUSE_PRESSED) {
      xOffset = event.getSceneX
      yOffset = event.getSceneY
    }
    else if (event.getEventType eq MouseEvent.MOUSE_DRAGGED) {
      //注意此处 api中有规定 正值向右移动，负值想左移动
      stage.setX(event.getScreenX - xOffset)
      if (event.getScreenY - yOffset < 0) stage.setY(0)
      else stage.setY(event.getScreenY - yOffset)
    }
  }
}
