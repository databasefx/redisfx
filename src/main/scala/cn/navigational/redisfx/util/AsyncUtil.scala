package cn.navigational.redisfx.util

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

object AsyncUtil {

  private val LOG: Logger = LoggerFactory.getLogger(AsyncUtil.getClass)

  /**
   * 不限时执行异步事件
   *
   * @param awaitable 待执行事件
   * @tparam T 执行返回结果数据类型
   * @return 返回执行结果
   */
  def awaitWithInf[T](awaitable: Awaitable[T]): T = {
    try {
      Await.result[T](awaitable, Duration.Inf)
    } catch {
      case ex: Exception =>
        LOG.error("Async event happen error", ex)
        throw ex
    }
  }

}
