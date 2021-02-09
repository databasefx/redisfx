package cn.navigational.redisfx.model

import scala.beans.BeanProperty

/**
 *
 * redis连接信息
 *
 * @author yangkui
 */
class RedisConnectInfo {
  /**
   * 服务器端口号
   */
  @BeanProperty
  var port: Int = _
  /**
   * 连接uuid
   */
  @BeanProperty
  var uuid: String = _
  /**
   * 连接名称
   */
  @BeanProperty
  var name: String = _
  /**
   * 服务器主机
   */
  @BeanProperty
  var host: String = _
  /**
   * redis密码
   */
  @BeanProperty
  var password: String = _
  /**
   * 最后使用日期
   */
  @BeanProperty
  var lastUseDate: String = _
  /**
   * 是否本地存储
   */
  @BeanProperty
  var localSave: Boolean = false

}
