package com.swordfall.streamingkafka

/**
  * @Author: Yang JianQiu
  * @Date: 2019/9/10 0:19
  */
object InternalRedisClient extends Serializable {

  @transient private var pool: JedisPool = null

}
