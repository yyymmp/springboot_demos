package com.jt.server.message.req;


import com.jt.server.message.PackageData;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 终端鉴权消息
 *
 * @author hylexus
 */
public class TerminalAuthenticationMsg extends PackageData {

    private String authCode;

    public TerminalAuthenticationMsg() {
    }

    public TerminalAuthenticationMsg(PackageData packageData) throws UnsupportedEncodingException {
        this();
        this.channel = packageData.getChannel();
        this.checkSum = packageData.getCheckSum();
        this.payload = packageData.getPayload();
        this.msgHeader = packageData.getMsgHeader();
        byte[] bs = new byte[packageData.getPayload().readableBytes()];
        packageData.getPayload().readBytes(bs);
        this.authCode = new String(bs, "GBK");
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getAuthCode() {
        return authCode;
    }

    @Override
    public String toString() {
        return "TerminalAuthenticationMsg [authCode=" + authCode + ", msgHeader=" + msgHeader +
                ", checkSum=" + checkSum + ", channel=" + channel + "]";
    }

}
