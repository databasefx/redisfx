package cn.navigational.redisfx.assets


import java.net.URL

object RedisFxResource {

  def load(path: String): URL = this.getClass.getResource(path)

}

