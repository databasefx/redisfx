package cn.navigational.redisfx

import cn.navigational.redisfx.controller.pane.RedisValTabController
import cn.navigational.redisfx.enums.RedisDataType
import javafx.event.Event
import javafx.scene.Node

import java.net.URL


/**
 *
 * 抽象redis 内容面板通用方法
 *
 * @author yangkui
 * @since 1.0
 */
abstract class AbstractRedisContentService[P <: Node, D](fxml: URL) extends AbstractFXMLController[P](fxml) {
  protected var valTabController: RedisValTabController = _

  /**
   * 父级Pane关闭时调用
   */
  def contentPaneRequestClose(event: Event): Unit

  /**
   * 内容发生改变时调用
   *
   * @param dataType 数据类型
   * @param data     外部传入数据
   */
  def contentUpdate(data: D, dataType: RedisDataType): Unit

}
