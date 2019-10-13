package com.swordfall.common

import java.time.Duration
import org.apache.kafka.clients.consumer.{Consumer, ConsumerConfig, KafkaConsumer, NoOffsetForPartitionException}
import org.apache.kafka.common.TopicPartition
import scala.collection.JavaConversions._
import scala.collection.mutable

object KafkaOffsetUtils {

  /**
    * 获取最小offset
    *
    * @param consumer   消费者
    * @param partitions topic分区
    * @return
    */
  def getEarliestOffsets(consumer: Consumer[_, _], partitions: Set[TopicPartition]): Map[TopicPartition, Long] = {
    consumer.seekToBeginning(partitions)
    partitions.map(tp => tp -> consumer.position(tp)).toMap
  }

  /**
    * 获取最小offset
    * Returns the earliest (lowest) available offsets, taking new partitions into account.
    *
    * @param kafkaParams kafka客户端配置
    * @param topics      获取offset的topic
    */
  def getEarliestOffsets(kafkaParams: Map[String, Object], topics: Iterable[String]): Map[TopicPartition, Long] = {
    val newKafkaParams = mutable.Map[String, Object]()
    newKafkaParams ++= kafkaParams
    newKafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    val consumer: KafkaConsumer[String, Array[Byte]] = new KafkaConsumer[String, Array[Byte]](newKafkaParams)
    consumer.subscribe(topics)
    val parts = consumer.assignment()
    consumer.seekToBeginning(parts)
    consumer.pause(parts)
    val offsets = parts.map(tp => tp -> consumer.position(tp)).toMap
    consumer.unsubscribe()
    consumer.close()
    offsets
  }

  /**
    * 获取最大offset
    *
    * @param consumer   消费者
    * @param partitions topic分区
    * @return
    */
  def getLatestOffsets(consumer: Consumer[_, _], partitions: Set[TopicPartition]): Map[TopicPartition, Long] = {
    consumer.seekToEnd(partitions)
    partitions.map(tp => tp -> consumer.position(tp)).toMap
  }

  /**
    * 获取最大offset
    * Returns the latest (highest) available offsets, taking new partitions into account.
    *
    * @param kafkaParams kafka客户端配置
    * @param topics      需要获取offset的topic
    **/
  def getLatestOffsets(kafkaParams: Map[String, Object], topics: Iterable[String]): Map[TopicPartition, Long] = {
    val newKafkaParams = mutable.Map[String, Object]()
    newKafkaParams ++= kafkaParams
    newKafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    val consumer: KafkaConsumer[String, Array[Byte]] = new KafkaConsumer[String, Array[Byte]](newKafkaParams)
    consumer.subscribe(topics)
    val parts = consumer.assignment()
    consumer.seekToEnd(parts)
    consumer.pause(parts)
    val offsets = parts.map(tp => tp -> consumer.position(tp)).toMap
    consumer.unsubscribe()
    consumer.close()
    offsets
  }

  /**
    * 获取消费者当前offset
    *
    * @param consumer   消费者
    * @param partitions topic分区
    * @return
    */
  def getCurrentOffsets(consumer: Consumer[_, _], partitions: Set[TopicPartition]): Map[TopicPartition, Long] = {
    partitions.map(tp => tp -> consumer.position(tp)).toMap
  }

  /**
    * 获取offsets
    *
    * @param kafkaParams kafka参数
    * @param topics      topic
    * @return
    */
  def getCurrentOffset(kafkaParams: Map[String, Object], topics: Iterable[String]): Map[TopicPartition, Long] = {
    val offsetResetConfig = kafkaParams.getOrElse(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest").toString.toLowerCase()
    val newKafkaParams = mutable.Map[String, Object]()
    newKafkaParams ++= kafkaParams
    newKafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none")
    val consumer: KafkaConsumer[String, Array[Byte]] = new KafkaConsumer[String, Array[Byte]](newKafkaParams)
    consumer.subscribe(topics)
    val notOffsetTopicPartition = mutable.Set[TopicPartition]()

    try {
      consumer.poll(Duration.ofMillis(0))
    } catch {
      case ex: NoOffsetForPartitionException  =>
        println(s"consumer topic partition offset not found:${ex.partition()}")
        notOffsetTopicPartition.add(ex.partition())
    }

    val parts = consumer.assignment().toSet
    consumer.pause(parts)
    val topicPartition = parts.diff(notOffsetTopicPartition)
    //获取当前offset
    val currentOffset = mutable.Map[TopicPartition, Long]()
    topicPartition.foreach(x => {
      try {
        currentOffset.put(x, consumer.position(x))
      } catch {
        case ex: NoOffsetForPartitionException =>
          println(s"consumer topic partition offset not found:${ex.partition()}")
          notOffsetTopicPartition.add(ex.partition())
      }
    })
    //获取earliiestOffset
    val earliestOffset = getEarliestOffsets(consumer, parts)
    earliestOffset.foreach(x => {
      val value = currentOffset.get(x._1)
      if (value.isEmpty){
        currentOffset(x._1) = x._2
      }else if (value.get < x._2){
        println(s"kafka data is lost from partition:${x._1} offset ${value.get} to ${x._2}")
        currentOffset(x._1) = x._2
      }
    })
    // 获取lastOffset
    val lateOffset = if (offsetResetConfig.equalsIgnoreCase("earliest")){
      getLatestOffsets(consumer, topicPartition)
    }else {
      getLatestOffsets(consumer, parts)
    }

    lateOffset.foreach(x => {
      val value = currentOffset.get(x._1)
      if (value.isEmpty || value.get > x._2){
        currentOffset(x._1) = x._2
      }
    })
    consumer.unsubscribe()
    consumer.close()
    currentOffset.toMap
  }
}
