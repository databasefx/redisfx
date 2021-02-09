package cn.navigational.redisfx.helper

import cn.navigational.redisfx.model.RedisConnectInfo
import cn.navigational.redisfx.util.JedisUtil
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{Jedis, JedisPool, JedisShardInfo}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object JedisHelper {
  /**
   * 测试redis是否能正常访问
   *
   * @param config redis配置信息
   * @return 返回ping结果
   */
  def pingRedis(config: RedisConnectInfo): Future[String] = Future {
    val shareInfo: JedisShardInfo = new JedisShardInfo(config.host, config.port)
    if (config.password.trim != "") {
      shareInfo.setPassword(config.password)
    }
    val jedis = new Jedis(shareInfo)
    try jedis.ping()
    finally if (jedis != null) jedis.close()
  }

  def newBuilder(connectInfo: RedisConnectInfo): JedisBuilder = {
    new JedisBuilder(connectInfo)
  }

  class JedisBuilder(val connectInfo: RedisConnectInfo) {

    def build(): JedisUtil = {
      val poolConfig = new GenericObjectPoolConfig()
      val jedisPool = if (connectInfo.password.trim != "") {
        new JedisPool(poolConfig, connectInfo.host, connectInfo.port, 10000, connectInfo.password)
      } else {
        new JedisPool(poolConfig, connectInfo.host, connectInfo.port)
      }
      new JedisUtil(jedisPool, connectInfo)
    }
  }

}
