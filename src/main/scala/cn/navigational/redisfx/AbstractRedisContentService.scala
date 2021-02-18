package cn.navigational.redisfx

import cn.navigational.redisfx.controller.pane.RedisValTabController
import cn.navigational.redisfx.enums.RedisDataType
import cn.navigational.redisfx.util.JedisUtil
import javafx.scene.Node

import java.net.URL
import scala.concurrent.Future


/**
 *
 * 抽象redis 内容面板通用方法
 *
 * @author yangkui
 * @since 1.0
 */
abstract class AbstractRedisContentService[P <: Node](val valTabController: RedisValTabController, fxml: URL) extends AbstractFXMLController[P](fxml) {

  /**
   * 父级Pane关闭时调用
   */
  def contentPaneRequestClose(): Unit = {}

  /**
   * 执行更新操作
   *
   * @param client jedis工具类
   * @param redisKey  key
   * @param index     数据库指数
   * @param dataType  redis数据类型
   * @return
   */
  def onContentUpdate(client: JedisUtil, redisKey: String, index: Int, dataType: RedisDataType): Future[Unit]
}
