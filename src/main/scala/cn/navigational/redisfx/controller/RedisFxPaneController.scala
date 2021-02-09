package cn.navigational.redisfx.controller

import cn.navigational.redisfx.AbstractViewController
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.RedisFxPaneController.{delete, jedisClients}
import cn.navigational.redisfx.controls.RedisClientTab
import cn.navigational.redisfx.helper.{JedisHelper, NotificationHelper}
import cn.navigational.redisfx.model.RedisConnectInfo
import cn.navigational.redisfx.util.JedisUtil
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.control.{ButtonType, Tab, TabPane}
import javafx.scene.layout.BorderPane
import javafx.stage.WindowEvent

import scala.jdk.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.{Await, Future, Promise}
import scala.util.control.Breaks.break
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


object RedisFxPaneController {
  private val fxPaneController: RedisFxPaneController = new RedisFxPaneController()
  private[RedisFxPaneController] val jedisClients: java.util.Map[String, JedisUtil] = new ConcurrentHashMap()

  def addRedisClient(connectInfo: RedisConnectInfo): Unit = {
    val uuid = connectInfo.getUuid
    if (jedisClients.containsKey(uuid)) {
      val tabs = fxPaneController.tabPane.getTabs.asScala
      for (elem <- tabs.toArray) {
        elem match {
          case tab: RedisClientTab =>
            val ud = tab.UUID
            if (!ud.equals(uuid)) break else this.switchTab(elem)
        }
      }
    } else {
      try {
        jedisClients.put(uuid, JedisHelper.newBuilder(connectInfo).build())
        val tab: Tab = new RedisClientTab(connectInfo)
        fxPaneController.tabPane.getTabs.add(tab)
        this.switchTab(tab)
      } catch {
        case ex: Exception =>
          this.delete(uuid)
          throw ex
      }
    }
  }

  def switchTab(tab: Tab): Unit = Platform.runLater { () =>
    fxPaneController.tabPane.getSelectionModel.select(tab)
    fxPaneController.openWindow()
  }

  def getRedisClient(uuid: String): JedisUtil = {
    jedisClients.get(uuid)
  }

  def delete(uuid: String): Future[Boolean] = Future {
    if (jedisClients.containsKey(uuid)) {
      val jedisUtil = jedisClients.get(uuid)
      jedisUtil.destroy()
      jedisClients.remove(uuid)
    }
    true
  }
}

class RedisFxPaneController extends AbstractViewController[BorderPane]("RedisFX", RedisFxResource.load("fxml/RedisFxPaneView.fxml")) {
  @FXML
  var tabPane: TabPane = _

  {
    this.setWindowSizeByProp(0.9, 0.8)
    tabPane.getTabs addListener new ListChangeListener[Tab] {
      override def onChanged(c: ListChangeListener.Change[_ <: Tab]): Unit = {
        val show: Boolean = tabPane.getTabs.size() > 0
        Platform.runLater(() => if (show) openWindow() else close())
      }
    }
  }

  override def onWindowRequestClose(event: WindowEvent): Unit = {
    val confirm = NotificationHelper.showConfirmAlert(msg = "你确定要关闭当前窗口?")
    if (!confirm) {
      return
    }
    event.consume()
    val promise = showLoad[Boolean]("关闭中...")
    synCloseHandler().onComplete {
      case Failure(ex) => promise.failure(ex)
      case Success(value) =>
        promise.success(value)
        maskPaneController.hidden()
        this.close()
    }
  }

  private def synCloseHandler() = Future[Boolean] {
    val arr = jedisClients.keySet().asScala.toArray
    for (elem <- arr) {
      Await.result(delete(elem), Duration.Inf)
      this.removeTab(elem)
    }
    true
  }

  private def removeTab(uuid: String): Unit = {
    val tabs = this.tabPane.getTabs
    var index: Int = -1
    for (j <- 0.until(tabs.size())) {
      val elem = tabs.get(j)
      if (!elem.isInstanceOf[RedisClientTab]) {
        return
      }
      val tab = elem.asInstanceOf[RedisClientTab]
      if (tab.UUID.equals(uuid)) {
        index = j
      }
    }
    if (index == -1) {
      return
    }
    Platform.runLater(() => tabs.remove(index))
  }
}