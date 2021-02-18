package cn.navigational.redisfx.controller.pane

import cn.navigational.redisfx.AbstractRedisContentService
import cn.navigational.redisfx.assets.RedisFxResource
import cn.navigational.redisfx.enums.RedisDataType
import cn.navigational.redisfx.util.{JSONUtil, JedisUtil, RedisDataUtil}
import javafx.scene.layout.BorderPane

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class FormContentPaneController(valTabController: RedisValTabController) extends AbstractRedisContentService[BorderPane](valTabController, RedisFxResource.load("fxml/pane/FormContentPane.fxml")) {
  /**
   * 执行更新操作
   *
   * @param client jedis工具类
   * @param redisKey  key
   * @param index     数据库指数
   * @param dataType  redis数据类型
   * @return
   */
  override def onContentUpdate(client: JedisUtil, redisKey: String, index: Int, dataType: RedisDataType): Future[Unit] = Future {
    val text = dataType match {
      case RedisDataType.HASH => Await.result[String](client.hGet(redisKey, index), Duration.Inf)
      case RedisDataType.LIST =>
        val len = Await.result[Long](client.lLen(redisKey, index), Duration.Inf)
        val list = Await.result[Array[String]](client.lRange(redisKey, 0, len, index), Duration.Inf)
        RedisDataUtil.formatListVal(list)
      case RedisDataType.SET =>
        val arr = Await.result[Array[String]](client.sMember(redisKey, index), Duration.Inf)
        JSONUtil.objToJsonStr(arr)
      case RedisDataType.Z_SET =>
        val len = Await.result[Long](client.zCard(redisKey, index), Duration.Inf)
        val arr = Await.result[Array[String]](client.zRange(redisKey, 0, len, index), Duration.Inf)
        RedisDataUtil.formatListVal(arr)
      case _ => Await.result[String](client.get(redisKey, index), Duration.Inf)
    }
  }
}
