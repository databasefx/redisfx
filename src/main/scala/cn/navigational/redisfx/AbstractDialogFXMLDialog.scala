package cn.navigational.redisfx

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.helper.AssetHelper
import javafx.scene.Parent
import javafx.scene.control.{Button, ButtonBar, Dialog, DialogPane}
import javafx.scene.layout.BorderPane

import java.net.URL

/**
 * 抽象fxml对话框
 *
 * @param url FXML 文件url
 */
class AbstractDialogFXMLDialog[R, P <: Parent](url: URL) extends AbstractFXMLController[P](url) {
  val dialog: Dialog[R] = new Dialog[R]
  val dialogPane: DialogPane = dialog.getDialogPane
  private val buttonBar: ButtonBar = new ButtonBar()
  private val rootPane: BorderPane = new BorderPane()


  {
    this.removeBtnBar()
    this.rootPane.setBottom(buttonBar)
    this.rootPane.setCenter(this.getParent)
    this.dialogPane.setContent(this.rootPane)
    this.dialogPane.getStylesheets.add(AssetHelper.APP_STYLE)
    this.dialogPane.getStylesheets.add(RedisFxResource.load("css/DialogStyle.css").toExternalForm)
  }

  private def removeBtnBar(): Unit = {
    val ch = this.dialogPane.getChildren
    val optional = ch
      .stream().filter(it => it.isInstanceOf[ButtonBar]).findAny()
    if (optional.isPresent) {
      ch.remove(optional.get())
    }
  }

  def addAction(bts: Button*): Unit = {
    for (el <- bts) {
      buttonBar.getButtons.add(el)
    }
  }

}
