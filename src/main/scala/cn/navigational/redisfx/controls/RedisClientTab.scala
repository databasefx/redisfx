package cn.navigational.redisfx.controls

import cn.navigational.redisfx.controller.RedisFxPaneController
import cn.navigational.redisfx.controller.pane.RedisClientTabPaneController
import cn.navigational.redisfx.model.RedisConnectInfo
import javafx.application.Platform
import javafx.scene.control.Tab

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class RedisClientTab(val connectInfo: RedisConnectInfo) extends Tab {
  private val tabPaneController = new RedisClientTabPaneController(connectInfo.uuid)

  {
    this.setContent(tabPaneController.getParent)
    this.setText(s"${connectInfo.getName}[${connectInfo.host}]")
    this.setOnCloseRequest(event => {
      event.consume()
      val promise = tabPaneController.showLoad("关闭中...")
      RedisFxPaneController.delete(UUID)
        .onComplete({
          case Success(_) => Platform.runLater(() => {
            val tablePane = getTabPane
            tablePane.getTabs.remove(this)
          })
          case Failure(ex) => promise.failure(ex)
        })
    })
  }

  def UUID: String = {
    connectInfo.uuid
  }
}
