## Spark读取配置文件有两种方式：

1.把配置文件config.properties放到java项目的resources目录下，直接把配置文件打到java项目里面，这种方式方便，缺点是配置修改之后需要重新打包，比较繁琐。

配置文件config.properties一般配置数据库ip、账户密码、kafka的接收ip等一些易变化的配置，如：
>#kafka配置
kafka.imsiWarningName=realTimeImsiWarning
`kafka.servers=192.168.31.244:9092`
kafka.imsiTopics=ImsiRecord
kafka.startingOffsets=latest
kafka.outputMode=append
kafka.processingTime=5 seconds
kafka.failOnDataLoss=false
#mongodb配置
`mongo.uri=mongodb://192.168.31.244:29017/meerkat-min`
mongo.connectionsPerHost=100
mongo.connectTimeout=1200000
mongo.maxWaitTime=30000
mongo.threadsAllowed=100
mongo.maxConnectionIdleTime=0
mongo.maxConnectionLifeTime=0
mongo.socketTimeout=0
mongo.socketKeepAlive=true

scala内部读取config.properties文件的代码，如下：
>val resource = ResourceBundle.getBundle("config"); //加载配置文件
    val appName = resource.getString("kafka.faceRecordName")
    val kafkaServers = resource.getString("kafka.servers")
    val faceTopics = resource.getString("kafka.faceTopics")
    val faceToCompareTopics = resource.getString("kafka.faceToCompareTopics")
    val startingOffsets = resource.getString("kafka.startingOffsets")
    val outputMode = resource.getString("kafka.outputMode")
    val processingTime = resource.getString("kafka.processingTime")
    val database = resource.getString("mongo.database")
