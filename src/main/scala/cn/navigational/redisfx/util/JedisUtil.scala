package cn.navigational.redisfx.util

import cn.navigational.redisfx.model.RedisConnectInfo
import redis.clients.jedis.{Jedis, JedisPool}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

class JedisUtil(private val jedisPool: JedisPool, val connectInfo: RedisConnectInfo) {
  def listDbCount(): Future[Int] = Future {
    val jedis = jedisPool.getResource
    try {
      val list = jedis.configGet("databases")
      list.get(1).toInt
    }
    finally if (jedis != null) jedis.close()
  }

  def lsAllKey(database: Int): Future[Array[String]] = {
    this.executeCommand[Array[String]](jedis => jedis.keys("*").asScala.toArray, database)
  }

  def get(key: String, database: Int): Future[String] = {
    this.executeCommand[String](jedis => jedis.get(key), database)
  }

  def del(key: String, database: Int): Future[Long] = {
    this.executeCommand[Long](jedis => jedis.del(key), database)
  }

  def ttl(key: String, database: Int): Future[Long] = {
    this.executeCommand[Long](jedis => jedis.ttl(key), database)
  }

  def setTtl(key: String, ttl: Int, database: Int): Future[Long] = {
    this.executeCommand[Long](jedis => jedis.expire(key, ttl), database)
  }

  def rename(oldKey: String, newKey: String, database: Int): Unit = {
    this.executeCommand[String](jedis => jedis.rename(oldKey, newKey), database)
  }

  def typeKey(key: String, database: Int): Future[String] = {
    this.executeCommand[String](jedis => jedis.`type`(key), database)
  }

  /**
   * 销毁Jedis连接池
   */
  def destroy(): Unit = {
    if (this.jedisPool.isClosed) {
      return
    }
    this.jedisPool.close()
  }

  /**
   * 使用高阶函数封装通用执行redis命令函数,
   *
   * @tparam T 执行完命令，返回数据类型
   * @return
   */
  private def executeCommand[T](executor: Jedis => T, database: Int): Future[T] = Future {
    val jedis = this.jedisPool.getResource
    try {
      jedis.select(database)
      executor.apply(jedis)
    } finally if (jedis != null) jedis.close()
  }
}