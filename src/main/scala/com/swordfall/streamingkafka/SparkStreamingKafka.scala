package com.swordfall.streamingkafka

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.codehaus.jackson.map.deser.std.StringDeserializer

/**
  * 获取topic最小的offset
  */
object SparkStreamingKafka {

  def main(args: Array[String]): Unit = {
    val brokers = "node1:9092"
    val topic = "1234"
    val partition: Int = 0 // 测试topic只有一个分区
    val start_offset: Long = 0L

    // Kafka参数
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "groud.id" -> "exactly-once",
      "enable.auto.commit" -> (false: java.lang.Boolean),
      "auto.offset.reset" -> "none"
    )

    // Redis configurations
    val maxTotal = 10
    val maxIdle = 10
    val minIdle = 1
    val redisHost = "192.168.187.201"
    val redisPort = 6379
    val redisTimeout = 30000
    // 默认db，用户存放Offset和pv数据
    val dbDefaultIndex = 8
    InternalRedisClient.makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle)

    val conf = new SparkConf().setAppName("SparkStreamingKafka").setIfMissing("spark.master", "local[2]")
    val ssc = new StreamingContext(conf, Seconds(10))

    // 从Redis获取上一次存的Offset
    val jedis = InternalRedisClient.getPool.getResource
    jedis.select(dbDefaultIndex)
    val topic_partition_key = topic + "_" + partition
    var lastOffset = 0L
    val lastSavedOffset = jedis.get(topic_partition_key)

    if (null != lastSavedOffset){
      try{
        lastOffset = lastSavedOffset.toLong
      }catch{
        case ex: Exception => println(ex.getMessage)
          println("get lastSavedOffset error, lastSavedOffset from redis [" + lastSavedOffset + "]")
          System.exit(1)
      }
    }
    InternalRedisClient.getPool.returnResource(jedis)

    println("lastOffset")
  }
}
