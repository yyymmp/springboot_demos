package com.jt.server.handler;

import static com.jt.server.consts.JT808Const.SERVER_RESP_COMMON;
import static com.jt.server.consts.JT808Const.SERVER_RESP_REGISTER;

import com.alibaba.fastjson.JSON;
import com.jt.server.codec.JT808Decoder;
import com.jt.server.codec.JT808Encoder;
import com.jt.server.message.MsgHeader;
import com.jt.server.message.PackageData;
import com.jt.server.message.req.LocationInfoUploadMsg;
import com.jt.server.message.req.TerminalAuthenticationMsg;
import com.jt.server.message.req.TerminalRegisterMsg;
import com.jt.server.message.resp.ServerCommonRespMsgBody;
import com.jt.server.message.resp.TerminalRegisterMsgRespBody;
import com.jt.server.mq.LocationProduct;
import com.jt.server.session.Session;
import com.jt.server.session.SessionManager;
import com.jt.server.uitls.BitOperator;
import com.jt.server.uitls.JT808ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jlz
 * @date 2022年10月19日 15:55
 */
public class TerminalMsgProcessService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final JT808Encoder jt808Encoder = new JT808Encoder();
    private final JT808ProtocolUtils jt808ProtocolUtils = new JT808ProtocolUtils();

    public void processRegisterMsg(TerminalRegisterMsg msg) throws Exception {
        log.debug("终端注册:{}", JSON.toJSONString(msg, true));

        final String sessionId = Session.buildId(msg.getChannel());
        Session session = sessionManager.findBySessionId(sessionId);
        if (session == null) {
            session = Session.buildSession(msg.getChannel(), msg.getMsgHeader().getTerminalPhone());
        }
        session.setAuthenticated(true);
        session.setTerminalPhone(msg.getMsgHeader().getTerminalPhone());
        sessionManager.put(session.getId(), session);

        TerminalRegisterMsgRespBody respMsgBody = new TerminalRegisterMsgRespBody();
        respMsgBody.setReplyCode(TerminalRegisterMsgRespBody.success);
        respMsgBody.setReplyFlowId(msg.getMsgHeader().getFlowId());
        //todo 授权码
        respMsgBody.setReplyToken("789654");

        ByteBuf respBody = ByteBufAllocator.DEFAULT.buffer();
        respBody.writeShort(msg.getMsgHeader().getFlowId()).writeShort(TerminalRegisterMsgRespBody.success).writeBytes("789654".getBytes("GBK"));

        ByteBuf byteBuf = genCommonResp(msg, SERVER_RESP_REGISTER, respBody);

        //// 1 消息id
        //respHeader.writeShort(SERVER_RESP_REGISTER);
        //int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(respBody.readableBytes(), 0b000, false, 0);
        //// 2 消息属性
        //respHeader.writeShort(msgBodyProps);
        //// 3 手机号
        //byte[] bytes = BitOperator.hexToByteArray(msg.getMsgHeader().getTerminalPhone());
        //respHeader.writeBytes(bytes);
        //// 4 流水号
        //respHeader.writeShort(msg.getMsgHeader().getFlowId());
        //respHeader.writeBytes(respBody);
        //
        ////标记数据取出之前
        //respHeader.markReaderIndex();
        //byte[] respArr = new byte[respHeader.readableBytes()];
        //respHeader.readBytes(respArr);
        ////校验码 注意此时respArr不能写入标识 否则标识会影响计算校验码 在下方escape转移中统一添加
        //int checkSum = BitOperator.getCheckSum4JT808(respArr, 0, respArr.length);
        //respHeader.writeByte(checkSum);
        //respHeader.resetReaderIndex();

        //添加标识并转义
        //ByteBuf escape = JT808Decoder.escape(respHeader);

        ChannelFuture future = msg.getChannel().writeAndFlush(byteBuf).sync();
        if (!future.isSuccess()) {
            log.error("注册消息回应 发送数据出错:{}", future.cause());
        }
        respBody.release();
        //respHeader.release();
    }

    public void processAuthMsg(TerminalAuthenticationMsg msg) throws InterruptedException {
        log.info("鉴权上报信息,鉴权码:{}", msg.getAuthCode());
        //假设每次鉴权都是成功得
        final String sessionId = Session.buildId(msg.getChannel());
        Session session = sessionManager.findBySessionId(sessionId);
        if (session == null) {
            session = Session.buildSession(msg.getChannel(), msg.getMsgHeader().getTerminalPhone());
        }
        session.setAuthenticated(true);
        session.setTerminalPhone(msg.getMsgHeader().getTerminalPhone());
        sessionManager.put(session.getId(), session);

        ByteBuf respBody = ByteBufAllocator.DEFAULT.buffer();
        respBody.writeShort(msg.getMsgHeader().getFlowId()).writeShort(msg.getMsgHeader().getMsgId()).writeByte(0);

        ByteBuf byteBuf = genCommonResp(msg, SERVER_RESP_COMMON, respBody);

        ChannelFuture future = msg.getChannel().writeAndFlush(byteBuf).sync();
        if (!future.isSuccess()) {
            log.error("鉴权消息回应 发送数据出错:{}", future.cause());
        }
        respBody.release();
    }

    /**
     * 兼容所有响应消息
     * @param msg
     * @param respBody
     */
    public ByteBuf genCommonResp(PackageData msg,short msgType,ByteBuf respBody){
        ByteBuf respHeader = ByteBufAllocator.DEFAULT.buffer();
        // 1 消息id
        respHeader.writeShort(msgType);
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(respBody.readableBytes(), 0b000, false, 0);
        // 2 消息属性
        respHeader.writeShort(msgBodyProps);
        // 3 手机号
        byte[] bytes = BitOperator.hexToByteArray(msg.getMsgHeader().getTerminalPhone());
        respHeader.writeBytes(bytes);
        // 4 流水号
        respHeader.writeShort(msg.getMsgHeader().getFlowId());
        //合并消息体
        respHeader.writeBytes(respBody);

        //标记数据取出之前
        respHeader.markReaderIndex();
        byte[] respArr = new byte[respHeader.readableBytes()];
        respHeader.readBytes(respArr);
        //校验码 注意此时respArr不能写入标识 否则标识会影响计算校验码 在下方escape转移中统一添加
        int checkSum = BitOperator.getCheckSum4JT808(respArr, 0, respArr.length);
        //写入校验码
        respHeader.writeByte(checkSum);
        respHeader.resetReaderIndex();

        //添加标识并转义
        return JT808Decoder.escape(respHeader);
    }

    public void processLocationInfoUploadMsg(LocationInfoUploadMsg msg) throws InterruptedException {
        log.info("位置上报信息:{}", msg);
        //发送至mq
        LocationProduct.send(msg);
        ByteBuf respBody = ByteBufAllocator.DEFAULT.buffer();
        respBody.writeShort(msg.getMsgHeader().getFlowId()).writeShort(msg.getMsgHeader().getMsgId()).writeByte(0);
        ByteBuf byteBuf = genCommonResp(msg, SERVER_RESP_COMMON, respBody);
        ChannelFuture future = msg.getChannel().writeAndFlush(byteBuf).sync();
        if (!future.isSuccess()) {
            log.error("位置消息回应 发送数据出错:{}", future.cause());
        }
        respBody.release();
    }


    public void processTerminalHeartBeatMsg(PackageData msg) throws InterruptedException {
        log.debug("心跳信息:{}", JSON.toJSONString(msg, true));
        ByteBuf respBody = ByteBufAllocator.DEFAULT.buffer();
        respBody.writeShort(msg.getMsgHeader().getFlowId()).writeShort(msg.getMsgHeader().getMsgId()).writeByte(0);
        ByteBuf byteBuf = genCommonResp(msg, SERVER_RESP_COMMON, respBody);
        ChannelFuture future = msg.getChannel().writeAndFlush(byteBuf).sync();
        if (!future.isSuccess()) {
            log.error("心跳消息回应 发送数据出错:{}", future.cause());
        }
        respBody.release();
    }
}
