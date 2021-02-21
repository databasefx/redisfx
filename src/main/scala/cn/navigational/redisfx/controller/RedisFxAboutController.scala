package cn.navigational.redisfx.controller

import cn.navigational.redisfx.AbstractViewController
import cn.navigational.redisfx.assets.RedisFxResource
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextArea

import java.io.InputStreamReader
import java.nio.charset.Charset
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RedisFxAboutController extends AbstractViewController("关于", RedisFxResource.load("fxml/RedisFxAboutView.fxml")) {
  @FXML
  private var textArea: TextArea = _

  {
    this.initText()
    this.setWindowSizeByProp(0.5, 0.7)
    this.getStage.showAndWait()
  }

  private def initText(): Unit = {
    Future {
      val sb = new StringBuilder()
      val in = RedisFxResource.load("About.txt").openStream()
      val reader = new InputStreamReader(in, Charset.forName("utf8"))
      val buffer = new Array[Char](1024)
      while (reader.read(buffer) != -1) {
        val str = new String(buffer)
        sb.append(str)
      }
      Platform.runLater(() => this.textArea.setText(sb.toString()))
    }
  }
}
