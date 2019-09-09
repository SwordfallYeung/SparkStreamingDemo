package com.swordfall.wordcount

import com.swordfall.common.LoggerLevels
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

/**
  * @author y15079
  * @create 2018-03-12 0:21
  * @desc spark streaming 数据累加
  **/
object StateFulWordCount {

  //String : 单词 hello
  //Seq[Int] : 这个批次某个单词的次数
  //Option[Int]：以前的结果，当前的

  //(hello, 1), (hello, 1), (tom, 1)
  //(hello, Seq(1,1)), (tom, Seq(1))
  //分好组的数据
  val updateFunc = (iter: Iterator[(String, Seq[Int], Option[Int])]) => {
    //iter.flatMap(it => Some(it._2.sum + it._3.getOrElse(0)).map(x => (it._1, x)))
    //iter.flatMap{case(x,y,z) => Some(y.sum + z.getOrElse(0)).map(m => (x, m))}
    //iter.map(t => (t._1, t._2.sum + t._3.getOrElse(0)))
    iter.map{case(word, current_count, history_count) => (word, current_count.sum + history_count.getOrElse(0))} //使用模式匹配，map要用大括号{}
  }

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    //StreamingContext
    val conf = new SparkConf().setAppName("StateFulWordCount").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))
    //updateStateByKey必须设置checkpointDir 否则启动不了
    sc.setCheckpointDir("d://ck")
    val ds = ssc.socketTextStream("192.168.187.201", 8888)
    //DStream是一个特殊的RDD
    //hello tom hello jerry
    val result = ds.flatMap(_.split(" ")).map((_, 1)).updateStateByKey(updateFunc, new HashPartitioner(sc.defaultParallelism), true)

    result.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
