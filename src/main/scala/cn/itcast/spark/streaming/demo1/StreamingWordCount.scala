package cn.itcast.spark.streaming.demo1

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * @author y15079
  * @create 2018-03-11 21:58
  * @desc
  **/
object StreamingWordCount {
  def main(args: Array[String]): Unit = {

    LoggerLevels.setStreamingLogLevels() //设置日志级别为warn，可以减少日志info级别输出打印，可以把这句注释后比较控制台日志输出
    //StreamingContext
    val conf = new SparkConf().setAppName("StreamingWordCount").setMaster("local[2]") //本地执行
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5)) //val ssc = new StreamingContext(conf, Seconds(5)) 可以直接不要SparkContext
    //接收数据
    val ds = ssc.socketTextStream("192.168.187.201", 8888) //监听主机192.168.187.201的8888端口的数据
    //DStream是一个特殊的RDD
    //hello tom hello jerry
    val result = ds.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_+_) //ds代表着一系列的连续的RDD
    //打印结果
    result.print()

    ssc.start()
    ssc.awaitTermination() //等待结束
    /*result.mapPartitions(it => {
      val connection
    })*/
  }
}
