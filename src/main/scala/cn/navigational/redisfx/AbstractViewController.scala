package cn.navigational.redisfx

import cn.navigational.redisfx.helper.AssetHelper
import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.scene.{Parent, Scene}
import javafx.stage.{Screen, Stage, WindowEvent}

import java.net.URL


class AbstractViewController[P <: Parent](fxmlUrl: URL) extends AbstractFXMLController[P](fxmlUrl) {
  private var stage: Stage = _
  private var title: String = "RedisFx"
  private val stackPane: StackPane = new StackPane()
  private val scene: Scene = new Scene(stackPane)

  {
    this.scene.getStylesheets.add(AssetHelper.APP_STYLE)
  }

  def this(title: String, fxmlUrl: URL) {
    this(fxmlUrl)
    this.title = title
  }

  /**
   * 关闭当前窗口
   */
  def close(): Unit = {
    if (stage == null) {
      return
    }
    Platform.runLater(() => stage.close())
  }

  /**
   * 初始化stage
   *
   * @return
   */
  def getStage: Stage = {
    if (stage == null) {
      stage = new Stage()
      stage.setScene(scene)
      stage.setTitle(this.title)
      this.stackPane.getChildren.add(0, getParent)
      stage.getIcons.addAll(AssetHelper.APP_ICON, AssetHelper.APP_ICON_2X)
      stage.setOnCloseRequest(this.onWindowRequestClose)
    }
    stage
  }

  /**
   * 打开窗口
   */
  def openWindow(await: Boolean = false): Unit = {
    val stage = getStage
    Platform.runLater(() => {
      if (stage.isShowing) {
        stage.toFront()
      } else {
        if (await) {
          stage.showAndWait()
        } else {
          stage.show()
        }
      }
    })
  }

  /**
   * 按照比例设置窗口宽度和高度
   *
   * @param wProp 宽度比例
   * @param hProp 高度比例
   */
  def setWindowSizeByProp(wProp: Double, hProp: Double): Unit = {
    val stage = this.getStage
    val rect = Screen.getPrimary.getBounds
    stage.setWidth(rect.getWidth * wProp)
    stage.setHeight(rect.getHeight * hProp)
  }

  /**
   * 当前窗口关闭事件触发该函数
   *
   * @param event 事件源
   */
  def onWindowRequestClose(event: WindowEvent): Unit = {
  }
}
