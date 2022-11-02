package com.jt.server.mq;

import com.alibaba.fastjson.JSONObject;
import com.jt.server.message.req.LocationInfoUploadMsg;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class LocationProduct {
    static DefaultMQProducer producer;
    static String namesrvAddr = "127.0.0.1:9876";
    static String group ="location-group";
    static String topic = "locationTopic";
    static String tags = "location";
    static {
        // 初始化一个producer并设置Producer group name
        producer = new DefaultMQProducer(group);
        // 设置NameServer地址
        producer.setNamesrvAddr(namesrvAddr);
        // 启动producer
        try {
            producer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        //重试次数
        producer.setRetryTimesWhenSendAsyncFailed(0);
    }

    /**
     * 发送信息
     *
     * @param uploadMsg
     */
    public static void send(LocationInfoUploadMsg uploadMsg) {
        try {
            String str = JSONObject.toJSONString(uploadMsg);
            Message msg = new Message(topic,
                    tags,
                    str.getBytes(RemotingHelper.DEFAULT_CHARSET));
            producer.send(msg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    System.out.printf("%-10d OK %s %n", sendResult,
                            sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    System.out.println("发送失败");
                    e.printStackTrace();
                }
            });
        } catch (UnsupportedEncodingException | MQClientException | InterruptedException | RemotingException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        LocationInfoUploadMsg locationInfoUploadMsg = new LocationInfoUploadMsg();
        locationInfoUploadMsg.setLatitude(211.0F);
        locationInfoUploadMsg.setLongitude(110.0F);
        locationInfoUploadMsg.setElevation(0);
        locationInfoUploadMsg.setSpeed(3.0F);
        locationInfoUploadMsg.setDirection(1);
        locationInfoUploadMsg.setTime(new Date());
        locationInfoUploadMsg.setWarningFlagField(0);
        locationInfoUploadMsg.setStatusField(0);
        send(locationInfoUploadMsg);
    }


}
