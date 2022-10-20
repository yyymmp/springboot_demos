package com.jt.server.codec;


import static com.jt.server.consts.JT808Const.PKG_DELIMITER;
import static com.jt.server.consts.JT808Const.SERVER_RESP_COMMON;
import static com.jt.server.consts.JT808Const.SERVER_RESP_REGISTER;

import com.jt.server.message.PackageData;
import com.jt.server.message.req.TerminalRegisterMsg;
import com.jt.server.message.resp.ServerCommonRespMsgBody;
import com.jt.server.message.resp.TerminalRegisterMsgRespBody;
import com.jt.server.session.Session;
import com.jt.server.uitls.BitOperator;
import com.jt.server.uitls.JT808ProtocolUtils;
import java.util.Arrays;

/**
 * 消息响应时 需要再次encode
 */
public class JT808Encoder {
    private BitOperator bitOperator;
    private JT808ProtocolUtils jt808ProtocolUtils;

    public JT808Encoder() {
        this.bitOperator = new BitOperator();
        this.jt808ProtocolUtils = new JT808ProtocolUtils();
    }

    public byte[] encode4TerminalRegisterResp(TerminalRegisterMsg req, TerminalRegisterMsgRespBody respMsgBody,
            int flowId) throws Exception {
        // 消息体字节数组
        byte[] msgBody = null;
        // 鉴权码(STRING) 只有在成功后才有该字段
        if (respMsgBody.getReplyCode() == TerminalRegisterMsgRespBody.success) {
            msgBody = this.bitOperator.concatAll(Arrays.asList(//
                    bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 流水号(2)
                    new byte[] { respMsgBody.getReplyCode() }, // 结果
                    respMsgBody.getReplyToken().getBytes("GBK")// 鉴权码(STRING)
            ));
        } else {
            msgBody = this.bitOperator.concatAll(Arrays.asList(//
                    bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 流水号(2)
                    new byte[] { respMsgBody.getReplyCode() }// 错误代码
            ));
        }

        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(req.getMsgHeader().getTerminalPhone(),
                SERVER_RESP_REGISTER, msgBody, msgBodyProps, flowId);
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);

        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }


    public byte[] encode4ServerCommonRespMsg(PackageData req, ServerCommonRespMsgBody respMsgBody, int flowId)
            throws Exception {
        byte[] msgBody = this.bitOperator.concatAll(Arrays.asList(//
                bitOperator.integerTo2Bytes(respMsgBody.getReplyFlowId()), // 应答流水号
                bitOperator.integerTo2Bytes(respMsgBody.getReplyId()), // 应答ID,对应的终端消息的ID
                new byte[] { respMsgBody.getReplyCode() }// 结果
        ));

        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(req.getMsgHeader().getTerminalPhone(),
                SERVER_RESP_COMMON, msgBody, msgBodyProps, flowId);
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBody);
        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }


    private byte[] doEncode(byte[] headerAndBody, int checkSum) throws Exception {
        byte[] noEscapedBytes = this.bitOperator.concatAll(Arrays.asList(//
                new byte[] { PKG_DELIMITER }, //0x7e
                headerAndBody, // 消息头+ 消息体
                bitOperator.integerTo1Bytes(checkSum), // 校验码
                new byte[] { PKG_DELIMITER }// 0x7e
        ));
        // 转义
        return jt808ProtocolUtils.doEscape4Send(noEscapedBytes, 1, noEscapedBytes.length - 2);
    }

}
