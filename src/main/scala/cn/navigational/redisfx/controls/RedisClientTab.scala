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
      tabPaneController.showLoad("关闭中...", func = () => {
        RedisFxPaneController.delete(UUID)
        val tablePane = getTabPane
        tablePane.getTabs.remove(this)
      })
    })
  }

  def UUID: String = {
    connectInfo.uuid
  }
}
