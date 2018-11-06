## Spark读取配置文件有两种方式：

1. 把配置文件config.properties放到java项目的resources目录下，直接把配置文件打到java项目里面，这种方式方便，缺点是配置修改之后需要重新打包，比较繁琐。

配置文件config.properties一般配置数据库ip、账户密码、kafka的接收ip等一些易变化的配置，如：
>#kafka配置<br/>
kafka.imsiWarningName=realTimeImsiWarning<br/>
`kafka.servers=192.168.31.244:9092`<br/>
kafka.imsiTopics=ImsiRecord<br/>
kafka.startingOffsets=latest<br/>
kafka.outputMode=append<br/>
kafka.processingTime=5 seconds<br/>
kafka.failOnDataLoss=false<br/>
#mongodb配置<br/>
`mongo.uri=mongodb://192.168.31.244:29017/meerkat-min`<br/>
mongo.connectionsPerHost=100<br/>
mongo.connectTimeout=1200000<br/>
mongo.maxWaitTime=30000<br/>
mongo.threadsAllowed=100<br/>
mongo.maxConnectionIdleTime=0<br/>
mongo.maxConnectionLifeTime=0<br/>
mongo.socketTimeout=0<br/>
mongo.socketKeepAlive=true<br/>

scala内部读取config.properties文件的代码，如下：
>val resource = ResourceBundle.getBundle("config"); //加载配置文件<br/>
val appName = resource.getString("kafka.faceRecordName")<br/>
val kafkaServers = resource.getString("kafka.servers")<br/>
val faceTopics = resource.getString("kafka.faceTopics")<br/>
val faceToCompareTopics = resource.getString("kafka.faceToCompareTopics")<br/>
val startingOffsets = resource.getString("kafka.startingOffsets")<br/>
val outputMode = resource.getString("kafka.outputMode")<br/>
val processingTime = resource.getString("kafka.processingTime")<br/>
val database = resource.getString("mongo.database")<br/>

2. 把配置文件config.properties放在部署该项目的外面，不打进java项目里面，直接通过spark submit的--files命令额外指定外面的配置文件，这种方式就是修改配置文件后，不需要重新编译打包。

配置文件config.properties如上所示：

scala外部读取config.properties文件的代码，如下：
