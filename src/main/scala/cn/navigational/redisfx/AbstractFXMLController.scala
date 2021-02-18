package cn.navigational.redisfx

import cn.navigational.redisfx.controller.pane.{MaskPaneBuilder, ViewMaskPaneController}
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.StackPane

import java.net.URL
import scala.concurrent.Promise
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 *
 * 抽象FXML视图控制器
 *
 * @author yangkui
 * @since 1.0
 */
abstract class AbstractFXMLController[P <: Node](fxmlUrl: URL) {
  var innerPane: P = _
  private val rootPane: StackPane = new StackPane()
  protected val maskPaneController: ViewMaskPaneController = new ViewMaskPaneController()


  {
    if (fxmlUrl == null) {
      throw new RuntimeException("fxmlUrl must can't null!")
    }
    this.initParent()
  }

  /**
   * 获取FXML对应的视图对象
   */
  def getParent: StackPane = {
    if (innerPane != null) {
      return rootPane
    }
    initParent()
  }

  /**
   * 初始化fxml视图
   *
   * @return
   */
  private def initParent(): StackPane = {
    try {
      val fxmlLoader = new FXMLLoader()
      fxmlLoader.setController(this)
      fxmlLoader.setLocation(fxmlUrl)
      innerPane = fxmlLoader.load[P]()
      rootPane.getChildren.add(innerPane)
      val option = initMaskPane()
      if (option.isDefined) {
        rootPane.getChildren.add(option.get)
      }
      rootPane
    } catch {
      case ex: Exception =>
        throw new RuntimeException(ex)
    }
  }

  /**
   * 子类可以覆盖此方法实现自定义MaskPane
   *
   * @return
   */
  protected def initMaskPane(): Option[Node] = {
    Option.apply(maskPaneController.parent)
  }

  /**
   * 显示加载层
   *
   * @param text      加载文字提示
   * @param errTitle  加载出错文字提示
   * @param showError 加载出错是否显示错误信息
   * @return 返回异步句柄
   */
  def showLoad[T](text: String = "加载中..", errTitle: String = "加载失败", showError: Boolean = true, showModel: Boolean = false): Promise[T] = {
    val builder = new MaskPaneBuilder()
      .showModel(showModel)
      .buildLoadPane(text, errTitle)

    maskPaneController.show(builder)
    val promise = Promise[T]()
    promise.future onComplete {
      case Success(_) => maskPaneController.hidden()
      case Failure(ex) =>
        if (showError) {
          this.showError(errTitle, ex)
        }
    }
    promise
  }

  /**
   * 显示错误Mask
   *
   * @param title 错误文字标题
   * @param ex    错误堆栈信息
   */
  def showError(title: String, ex: Throwable): Unit = {
    val builder = new MaskPaneBuilder()
      .showModel(true)
      .builderErrorPane(title, ex)
    maskPaneController.show(builder)
  }
}
