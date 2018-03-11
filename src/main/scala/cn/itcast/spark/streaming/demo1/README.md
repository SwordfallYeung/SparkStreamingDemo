# netcat安装并发送数据到Spark Streaming上
使用VMware上的虚拟机192.168.187.201，安装netcat，使用命令"nc -lk 8888"，开启socketServer服务器，通过8888端口发送数据，而spark streaming则通过val ds = ssc.socketTextStream("192.168.187.201", 8888)监听192.168.187.201上的8888端口获取数据<br/> 
netcat安装参考：http://blog.csdn.net/paicmis/article/details/53431163<br/>
spark streaming官网翻译：http://www.cnblogs.com/swordfall/p/8378000.html<br/>
