package cn.navigational.redisfx.util

import cn.navigational.redisfx.enums.{RedisDataType, RedisReplyStatus}
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

  def lsKey(pattern: String, database: Int): Future[Array[String]] = {
    this.executeCommand[Array[String]](jedis => jedis.keys(pattern).asScala.toArray, database)
  }

  def get(key: String, database: Int): Future[String] = {
    this.executeCommand[String](jedis => jedis.get(key), database)
  }

  def hGet(key: String, database: Int): Future[java.util.Map[String, String]] = {
    this.executeCommand[java.util.Map[String, String]](jedis => jedis.hgetAll(key), database)
  }

  /**
   * 向redis数组追加元素
   *
   * @param key      arr name
   * @param value    arr value
   * @param database target database
   * @return
   */
  def lPush(key: String, value: String, database: Int): Future[Boolean] = {
    this.executeCommand[Boolean](jedis => jedis.lpush(key, value) >= 0, database)
  }

  /**
   * 写入无序集合值
   *
   * @param key      集合key
   * @param value    集合值
   * @param database 目标数据库
   * @return
   */
  def sAdd(key: String, value: String, database: Int): Future[Boolean] = {
    this.executeCommand[Boolean](jedis => jedis.sadd(key, value) > 0, database)
  }

  /**
   * 写入有序集合值
   *
   * @param scope 权重值
   * @return
   */
  def zAdd(key: String, value: String, scope: Int, database: Int): Future[Boolean] = {
    this.executeCommand[Boolean](jedis => jedis.zadd(key, scope, value) > 0, database)
  }

  def sMember(key: String, database: Int): Future[Array[String]] = {
    this.executeCommand[Array[String]](jedis => jedis.smembers(key).asScala.toArray, database)
  }

  def zCard(key: String, database: Int): Future[Long] = {
    this.executeCommand(jedis => jedis.zcard(key), database)
  }

  def zRange(key: String, start: Long, end: Long, database: Int): Future[Array[String]] = {
    this.executeCommand[Array[String]](jedis => jedis.zrange(key, start, end).asScala.toArray, database)
  }

  /**
   * 获取指定key数组长度
   *
   * @param key
   * @param database
   * @return
   */
  def lLen(key: String, database: Int): Future[Long] = {
    this.executeCommand[Long](jedis => jedis.llen(key), database)
  }

  /**
   * 获取redis数组元素
   *
   * @param key   目标key
   * @param start 数组坐标起始位置
   * @param end   数组结束位置
   * @return 返回数组值列表
   */
  def lRange(key: String, start: Long, end: Long, database: Int): Future[Array[String]] = {
    this.executeCommand(jedis => jedis.lrange(key, start, end).asScala.toArray, database)
  }

  /**
   * 设置普通字符串值
   *
   * @param key      redis key
   * @param value    redis value
   * @param database target database
   * @param ttl      expire time
   * @return
   */
  def setEx(key: String, value: String, database: Int, ttl: Int = -1): Future[Boolean] = {
    this.executeCommand[Boolean](jedis => {
      val status = if (ttl < 0) {
        jedis.set(key, value)
      } else {
        jedis.setex(key, ttl, value)
      }
      status.equals(RedisReplyStatus.OK.toString)
    }, database)
  }

  def hSet(key: String, field: String, value: String, database: Int): Future[Boolean] = {
    this.executeCommand[Boolean](jedis => jedis.hset(key, field, value) > 0, database)
  }

  def hSet(key: String, attr: java.util.Map[String, String], database: Int): Future[Boolean] = {
    this.executeCommand[Boolean](jedis => jedis.hset(key, attr) > 0, database)
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

  def typeKey(key: String, database: Int): Future[RedisDataType] = {
    this.executeCommand[RedisDataType](jedis => RedisDataType.getDataType(jedis.`type`(key)), database)
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