package cn.navigational.redisfx.helper

import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.{Alert, ButtonType, TextInputDialog}
import javafx.scene.control.Alert.AlertType
import org.controlsfx.control.Notifications

import java.util.Optional

object NotificationHelper {
  def showInfo(text: String, pos: Pos): Unit = Platform.runLater(() => {
    Notifications.create().text(text).position(pos).showInformation()
  })


  def showConfirmAlert(title: String = "确认", msg: String): Boolean = {
    val alert = this.createAlert(AlertType.CONFIRMATION, title, msg)
    val optional = alert.showAndWait()
    optional.isPresent && optional.get() != ButtonType.CANCEL
  }

  def showInputDialog(title: String): Optional[String] = {
    val dialog = new TextInputDialog()
    dialog.setTitle("输入框")
    dialog.setContentText(title)
    dialog.getDialogPane.getStylesheets.add(AssetHelper.APP_STYLE)
    dialog.showAndWait()
  }


  def createAlert(alertType: AlertType, title: String = "", msg: String): Alert = {
    val alert = new Alert(alertType)
    alert.setTitle(title)
    alert.setContentText(msg)
    alert.getDialogPane.getStylesheets.add(AssetHelper.APP_STYLE)
    alert
  }
}
