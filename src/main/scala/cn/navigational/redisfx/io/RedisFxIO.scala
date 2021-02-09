package cn.navigational.redisfx.io

import cn.navigational.redisfx.AppPlatform
import cn.navigational.redisfx.model.RedisConnectInfo
import cn.navigational.redisfx.util.JSONUtil

import java.io.File
import java.nio.file.{Files, Path}
import java.util
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object RedisFxIO {
  private val REDIS_CONNECT_FILE = new File(s"${AppPlatform.getApplicationDataFolder}${File.separator}redis_connect_info.json")

  /**
   * 持久化redis连接信息
   *
   * @return 异步句柄
   */
  def saveConnectInfo(info: RedisConnectInfo): Future[Unit] = Future {
    val arr = Await.result(getConnectFile, Duration.Inf)
    arr :+ info
    Await.result[Boolean](this.saveConnectFile(arr), Duration.Inf)
  }

  /**
   * 创建连接文件
   */
  def getConnectFile: Future[Array[RedisConnectInfo]] = Future {
    val path = Path.of(REDIS_CONNECT_FILE.toURI)
    val arr: ArrayBuffer[RedisConnectInfo] = new ArrayBuffer[RedisConnectInfo]()
    if (!REDIS_CONNECT_FILE.exists()) {
      Files.createFile(path)
      Files.write(path, "[]".getBytes())
    } else {
      val bytes = Files.readAllBytes(path)
      val list = JSONUtil.strToArr(new String(bytes), classOf[RedisConnectInfo])
      list.forEach(it => arr.addOne(it))
    }
    arr.toArray
  }

  /**
   * 保存连接信息
   *
   * @param arr 连接信息集合
   */
  def saveConnectFile(arr: Array[RedisConnectInfo]): Future[Boolean] = Future {
    val path = Path.of(REDIS_CONNECT_FILE.toURI)
    val list: util.List[RedisConnectInfo] = new util.ArrayList()
    for (element <- arr) {
      list.add(element)
    }
    Files.writeString(path, JSONUtil.objToJson(list))
    true
  }
}
