package com.jt.server.message;

import com.alibaba.fastjson.annotation.JSONField;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.util.Arrays;

/**
 * 数据包
 * 消息头 + 消息体 + 校验码
 */
public class PackageData {
    /**
     * 16byte 消息头
     */
    protected MsgHeader msgHeader;

    // 消息体字节数组
    @JSONField(serialize = false)
    protected ByteBuf payload;

    /**
     * 校验码 1byte
     */
    protected int checkSum;

    @JSONField(serialize = false)
    protected Channel channel;

    public MsgHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MsgHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "PackageData [msgHeader=" + msgHeader + ", msgBodyBytes=" + Arrays.toString(payload.array()) + ", checkSum="
                + checkSum + ", address=" + channel + "]";
    }
}
