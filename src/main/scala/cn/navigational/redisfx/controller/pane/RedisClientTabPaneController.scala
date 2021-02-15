package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractFXMLController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.{AddRedisKeyController, RedisFxPaneController}
import cn.navigational.redisfx.controls.{RedisDatabaseItem, RedisFolderItem, RedisFxTreeItem, RedisKeyTreeItem, RedisValTab}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{TabPane, TreeItem, TreeView}

import scala.concurrent.ExecutionContext.Implicits.global
import java.util
import scala.concurrent.Future
import scala.util.{Failure, Success}


class RedisClientTabPaneController(val uuid: String) extends AbstractFXMLController[TabPane](RedisFxResource.load("fxml/pane/RedisClientTabPane.fxml")) {

  @FXML
  private var tabPane: TabPane = _

  @FXML
  private var treeView: TreeView[String] = _

  {
    this.loadAllDB()
    this.treeView.setOnMouseClicked(e => {
      val selectItem = this.treeView.getSelectionModel.getSelectedItem
      if (e.getClickCount >= 2 && selectItem != null && selectItem.isLeaf) {
        selectItem match {
          case rk: RedisKeyTreeItem =>
            if (rk.isLeaf) {
              val key = rk.getRowKey
              val optional = tabPane.getTabs
                .stream().filter(it => it.asInstanceOf[RedisValTab].redisKey == key).findAny()
              val tab = if (optional.isEmpty) {
                val tab = new RedisValTab(key, uuid, rk.index) {
                  this.treeItem = selectItem
                  this.clientTabPaneController = RedisClientTabPaneController.this
                }
                tabPane.getTabs.add(tab)
                tab
              } else {
                optional.get()
              }
              tabPane.getSelectionModel.select(tab)
            }
          case rk: RedisDatabaseItem =>
          case rf: RedisFolderItem =>
        }
      }
    })
  }


  @FXML
  def refresh(): Unit = {
    if (treeView.getRoot.getChildren.isEmpty) {
      this.loadAllDB()
    } else {
      currentTreeItem().foreach(target => target.refreshEvent())
    }
  }

  @FXML
  def deleteKey(): Unit = {
    val option = currentTreeItem()
    if (option.isEmpty) {
      return
    }
    val target = option.get
    val future = target.deleteEvent(uuid)
    future onComplete {
      case Success(rs) =>
        if (rs) {
          Platform.runLater(() => this.delTreeItem(target))
        }
    }
  }

  @FXML
  def addRedisKey(): Unit = {
    new AddRedisKeyController(this)
  }

  /**
   * 递归删除空节点
   *
   * @param item 目标节点
   */
  def delTreeItem(item: TreeItem[String]): Unit = {
    var current = item
    var parent = item.getParent
    while (parent != null) {
      val arr = parent.getChildren
      arr.remove(current)
      if (arr.isEmpty && parent.getParent != this.treeView.getRoot) {
        current = parent
        parent = parent.getParent
      } else {
        return
      }
    }
  }

  /**
   * 获取当前选中项目
   */
  private def currentTreeItem(): Option[RedisFxTreeItem] = {
    val selectItem = this.treeView.getSelectionModel.getSelectedItem
    if (selectItem == null || !selectItem.isInstanceOf[RedisFxTreeItem]) {
      Option.empty
    } else {
      Option.apply(selectItem.asInstanceOf[RedisFxTreeItem])
    }
  }

  private def loadAllDB(): Unit = {
    val promise = showLoad[Unit]("加载DB索引中...")
    val rootNode = treeView.getRoot
    val future: Future[Int] = RedisFxPaneController.getRedisClient(uuid).listDbCount()

    future onComplete {
      case Success(count) =>
        promise.success(count)
        val arr = new util.ArrayList[RedisDatabaseItem]()
        for (i <- 0 until count) {
          arr.add(new RedisDatabaseItem(i, uuid))
        }
        Platform.runLater(() => rootNode.getChildren.addAll(arr))
      case Failure(ex) => promise.failure(ex)
    }
  }

}
