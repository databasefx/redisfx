package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.controller.pane.MaskPaneStatus.{ERROR, INFO, LOADING, MaskPaneStatus}
import javafx.application.Platform
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.{Button, Label, ProgressIndicator, TextArea}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, HBox, Priority, VBox}

import java.io.{PrintWriter, StringWriter}
import java.time.Duration
import java.util.{Timer, TimerTask}

/**
 *
 * 枚举视图遮罩层状态
 *
 */
object MaskPaneStatus extends Enumeration {
  type MaskPaneStatus = Value
  val INFO, ERROR, LOADING, CONFIRM = Value
}

class MaskPaneBuilder() {
  //错误Mask错误信息
  var ex: Throwable = _
  //加载时显示文字信息
  var loadText: String = _
  //错误标题
  var errTitle: String = _
  //显示状态
  var status: MaskPaneStatus = _
  //是否显示关闭按钮
  var canClose: Boolean = false
  //是否显示遮罩层
  var model: Boolean = false

  def buildLoadPane(text: String, errTitle: String): MaskPaneBuilder = {
    this.status = LOADING
    this.loadText = text
    this.errTitle = errTitle
    this
  }

  def showModel(model: Boolean): MaskPaneBuilder = {
    this.model = model
    this
  }

  def builderErrorPane(text: String, ex: Throwable): MaskPaneBuilder = {
    this.canClose = true
    this.ex = ex
    this.status = ERROR
    this.errTitle = text
    this
  }
}


/**
 *
 *
 * @author yangkui
 * @since 1.0
 */
class ViewMaskPaneController {

  private object ViewMaskPaneConstant {
    val DEFAULT_LOAD_CLASS: String = "mask-load-pane"
    val DEFAULT_ERROR_CLASS: String = "mask-error-pane"
    val DEFAULT_MASK_MODEL_CLASS: String = "mask-pane-model"
    val ERROR_ICON: Image = new Image(RedisFxResource.load("icon/mask/error.png").openStream())
  }

  @FXML
  private var closeBtn: Button = _

  private var timer: Timer = _

  private var builder: MaskPaneBuilder = _

  val parent: BorderPane = {
    val fxmlLoader = new FXMLLoader()
    fxmlLoader.setController(this)
    fxmlLoader.setLocation(RedisFxResource.load("fxml/pane/ViewMaskPane.fxml"))
    fxmlLoader.load()
  }


  /**
   * 在当前场景图中显示MaskPane并在给定时间内自动关闭MaskPane
   *
   * @param duration 关闭MaskPane时长
   */
  def show(builder: MaskPaneBuilder, duration: Duration): Unit = {
    this.show(builder)
    timer = new Timer()
    val delay = duration.toMillis
    timer.schedule(new TimerTask() {
      override def run(): Unit = {
        hidden()
      }
    }, delay)
    this.reBuilder()
  }

  /**
   * 在当前场景图中显示MaskPane
   */
  def show(builder: MaskPaneBuilder): Unit = {
    if (timer != null) {
      timer.cancel()
    }
    this.builder = builder
    this.reBuilder()
    this.parent.setVisible(true)
  }

  /**
   * 隐藏当前MaskPane
   */
  def hidden(): Unit = {
    if (timer != null) {
      timer.cancel()
    }
    Platform.runLater(() => this.parent.setVisible(false))
  }

  private def reBuilder(): Unit = Platform.runLater(() => {
    if (builder == null) {
      return
    }
    val status = builder.status
    Platform.runLater(() => {
      if (status == LOADING) {
        this.parent.setCenter(new MaskLoadPane(builder.loadText))
      }
      if (status == ERROR) {
        this.parent.setCenter(new MaskErrorPane(builder.errTitle, builder.ex))
      }
      val styleClass = this.parent.getStyleClass
      val modelClass = ViewMaskPaneConstant.DEFAULT_MASK_MODEL_CLASS
      if (builder.model) {
        if (!styleClass.contains(modelClass)) {
          styleClass.add(modelClass)
        }
      } else {
        styleClass.remove(modelClass)
      }
      this.closeBtn.setVisible(builder.canClose)
    })
  })

  /**
   * 默认加载Pane
   *
   * @param text 自定义显示文字
   */
  class MaskLoadPane(val text: String) extends VBox {
    val container: HBox = new HBox()
    val label: Label = new Label(text)
    val process: ProgressIndicator = new ProgressIndicator()

    {
      val temp = new VBox()
      this.getChildren.add(container)
      this.container.getChildren.add(temp)
      temp.getChildren.addAll(process, label)
      this.getStyleClass.add(ViewMaskPaneConstant.DEFAULT_LOAD_CLASS)
    }
  }

  /**
   * 默认错误Pane
   *
   * @param title 错误提示信息
   * @param ex    错误堆栈信息
   */
  class MaskErrorPane(val title: String, ex: Throwable) extends VBox {
    val label = new Label(title)
    val topBox: HBox = new HBox()
    val textArea: TextArea = new TextArea()
    val icon = new ImageView(ViewMaskPaneConstant.ERROR_ICON)

    {
      val sw = new StringWriter
      val pw = new PrintWriter(sw)
      try ex.printStackTrace(pw)
      finally if (pw != null) pw.close()
      this.textArea.setEditable(false)
      this.textArea.setText(sw.toString)
      VBox.setVgrow(textArea, Priority.ALWAYS)
      this.topBox.getChildren.addAll(icon, label)
      this.getChildren.addAll(topBox, textArea)
      this.getStyleClass.add(ViewMaskPaneConstant.DEFAULT_ERROR_CLASS)
    }
  }

}
