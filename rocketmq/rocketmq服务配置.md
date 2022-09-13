安装包下载：
https://www.apache.org/dyn/closer.cgi?path=rocketmq/4.9.4/rocketmq-all-4.9.4-bin-release.zip

需要配置系统环境变量：
- ROCKETMQ_HOME   D:\service\rocketmq-all-4.9.4-bin-release 
- JAVA_HOME jdk环境变量 D:\soft\install\Java\jdk1.8.0_201

启动步骤：
- 启动NameServer：mqnamesrv
- 启动Broker： mqbroker

顺序消费的实现：
生产顺序性和消费顺序，只有同时满足了生产顺序性和消费顺序性才能达到上述的FIFO效果。
生产者顺序性：消息生产的顺序性仅支持单一生产者，不同生产者分布在不同的系统，即使设置相同的分区键，不同生产者之间产生的消息也无法判定其先后顺序。 还需要串行发送
在消息发送时，通过固定规则分片实现分区顺序消费，同一个ShardingKey的消息会被分配到同一个队列中，并按照顺序被消费。 参见com.rocket.produce.Producer

如果一个Broker掉线，那么此时队列总数是否会发化？
如果发生变化，那么同一个 ShardingKey 的消息就会发送到不同的队列上，造成乱序。如果不发生变化，那消息将会发送到掉线Broker的队列上，必然是失败的。