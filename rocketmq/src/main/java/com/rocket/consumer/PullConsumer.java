package com.rocket.consumer;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.consumer.PullStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PullConsumer {
    /**
     * 在RocketMQ中有两种Pull方式，一种是比较原始Pull Consumer，它不提供相关的订阅方法，需要调用pull方法时指定队列进行拉取，并需要自己更新位点。
     * 另一种是Lite Pull Consumer，它提供了Subscribe和Assign两种方式，使用起来更加方便。
     * @param args
     * @throws MQClientException
     */
    public static void main(String[] args) throws MQClientException {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("please_rename_unique_group_name_5");
        consumer.setNamesrvAddr("127.0.0.1:9876");
        consumer.start();
        try {
            MessageQueue mq = new MessageQueue();
            mq.setQueueId(0);
            mq.setTopic("TopicTest");
            mq.setBrokerName("jinrongtong-MacBook-Pro.local");
            long offset = 26;
            PullResult pullResult = consumer.pull(mq, "*", offset, 32);
            if (pullResult.getPullStatus().equals(PullStatus.FOUND)) {
                System.out.printf("%s%n", pullResult.getMsgFoundList());
                consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumer.shutdown();
    }
}
//Subscribe模式
class LitePullConsumerSubscribe {
    public static volatile boolean running = true;
    public static void main(String[] args) throws Exception {
        DefaultLitePullConsumer litePullConsumer = new DefaultLitePullConsumer("lite_pull_consumer_test");
        litePullConsumer.subscribe("TopicTest", "*");
        //每一次拉取的最大消息数量
        litePullConsumer.setPullBatchSize(20);
        litePullConsumer.start();
        try {
            while (running) {
                List<MessageExt> messageExts = litePullConsumer.poll();
                System.out.printf("%s%n", messageExts);
            }
        } finally {
            litePullConsumer.shutdown();
        }
    }
}

class LitePullConsumerAssign {
    public static volatile boolean running = true;
    public static void main(String[] args) throws Exception {
        DefaultLitePullConsumer litePullConsumer = new DefaultLitePullConsumer("please_rename_unique_group_name");
        //，这里我们采用手动提交位点的方式，因此设置AutoCommit为false
        litePullConsumer.setAutoCommit(false);
        litePullConsumer.start();
        //，先用fetchMessageQueues获取了Topic下的队列，再
        Collection<MessageQueue> mqSet = litePullConsumer.fetchMessageQueues("TopicTest");
        List<MessageQueue> list = new ArrayList<>(mqSet);
        List<MessageQueue> assignList = new ArrayList<>();
        //取前面的一半队列进行拉取
        for (int i = 0; i < list.size() / 2; i++) {
            assignList.add(list.get(i));
        }
        litePullConsumer.assign(assignList);
        //例中还调用了seek方法，将第一个队列拉取的位点设置从10开始。
        litePullConsumer.seek(assignList.get(0), 10);
        try {
            while (running) {
                List<MessageExt> messageExts = litePullConsumer.poll();
                System.out.printf("%s %n", messageExts);
                litePullConsumer.commitSync();
            }
        } finally {
            litePullConsumer.shutdown();
        }
    }
}
