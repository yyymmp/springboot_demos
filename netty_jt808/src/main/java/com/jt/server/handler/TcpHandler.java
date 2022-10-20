package com.jt.server.handler;

import static com.jt.server.consts.JT808Const.TERNIMAL_MSG_AUTH;
import static com.jt.server.consts.JT808Const.TERNIMAL_MSG_LOCATION;
import static com.jt.server.consts.JT808Const.TERNIMAL_MSG_REGISTER;

import com.jt.server.codec.JT808Decoder;
import com.jt.server.consts.JT808Const;
import com.jt.server.message.MsgHeader;
import com.jt.server.message.PackageData;
import com.jt.server.message.req.LocationInfoUploadMsg;
import com.jt.server.message.req.TerminalAuthenticationMsg;
import com.jt.server.message.req.TerminalRegisterMsg;
import com.jt.server.uitls.BitOperator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TcpHandler extends ChannelInboundHandlerAdapter {
    Logger logger = LoggerFactory.getLogger(ChannelInboundHandlerAdapter.class);

    private final JT808Decoder decoder = new JT808Decoder();

    private final TerminalMsgProcessService terminalMsgProcessService =  new TerminalMsgProcessService();
    public static byte bcc(ByteBuf byteBuf, int tailOffset) {
        byte cs = 0;
        int readerIndex = byteBuf.readerIndex();
        int writerIndex = byteBuf.writerIndex() + tailOffset;
        while (readerIndex < writerIndex) {
            cs ^= byteBuf.getByte(readerIndex++);
        }
        return cs;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            logger.debug("<<<<< ip:{},hex:{}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(buf));
            if (buf.readableBytes() <= 0) {
                logger.error("无可读字节，返回");
                // ReferenceCountUtil.safeRelease(msg);
                return;
            }
            PackageData packageData = new PackageData();
            //验证校验码
            byte checkCode = bcc(buf, -1);
            byte exist = buf.getByte(buf.writerIndex() - 1);
            if (exist != checkCode) {
                logger.error("校验码错误：{}，{}", checkCode, exist);
            }

            MsgHeader msgHeader = new MsgHeader();
            //转义 0x7e
            ByteBuf escape = JT808Decoder.unescape(buf);
            System.out.println("写指针： " + escape.writerIndex());
            int msgId = escape.readUnsignedShort();
            logger.info("消息id：{}", msgId);
            msgHeader.setMsgId(msgId);
            int prop = escape.readUnsignedShort();
            //消息属性 2字节
            msgHeader.setMsgBodyPropsField(prop);

//            如何兼容2019版本
//            最新协议文档已经写好了如何做兼容，就是在消息体属性中第14位为版本标识。
//            当第14位为0时，标识协议为2011年的版本；
//            当第14位为1时，标识协议为2019年的版本。
            logger.info("消息属性：{}", prop);
            // [ 0-9 ] 0000,0011,1111,1111(3FF)(消息体长度)
            logger.info("长度：{}", prop & 0x01ff);
            msgHeader.setMsgBodyLength(prop & 0x01ff);
            // [10-12] 0001,1100,0000,0000(1C00)(加密类型)
            logger.info("加密类型：{}", (prop & 0x1c00) >> 10);
            msgHeader.setEncryptionType((prop & 0x1c00) >> 10);
            // [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
            logger.info("是否有子包：{}", ((prop & 0x2000) >> 13) == 1);
            msgHeader.setHasSubPackage(((prop & 0x2000) >> 13) == 1);
            // [14-15] 1100,0000,0000,0000(C000)(保留位)
            logger.info("保留位：{}", ((prop & 0xc000) >> 14) + "");
            msgHeader.setReservedBit(((prop & 0xc000) >> 14) + "");
            //单独校验14位版本
            // [14-] 0100,0000,0000,0000(C000)(14保留位  版本标识)
            int version = (prop & 0x4000) >> 14;
            msgHeader.setVersion(version);
            String tel;
            if (version == 0) {
                logger.info("2013版本");
                byte[] telBytes = new byte[6];
                //手机号直接由十六进制字符串保存 所以此处不能根据字节数组转成字符串 直接转成hex字符串即可
                escape.readBytes(telBytes);
                tel = BitOperator.bytesToHex(telBytes);
                logger.info("手机号：{}", tel);
            } else {
                logger.info("2019版本");
                //协议版本号
                byte b = escape.readByte();
                System.out.println("协议版本号: "+b);
                //读取手机号 [5-15)
                byte[] telBytes = new byte[10];
                escape.readBytes(telBytes);
                tel = BitOperator.bytesToHex(telBytes);
                logger.info("手机号：{}", tel);
            }
            msgHeader.setTerminalPhone(tel);
            //读取消息流水号
            int flowId = escape.readUnsignedShort();
            logger.info("消息流水号：{}", flowId);
            msgHeader.setFlowId(flowId);
            // 5. 消息包封装项
            // 有子包信息
            if (msgHeader.isHasSubPackage()) {
                // 消息包封装项字段
                msgHeader.setPackageInfoField(escape.readUnsignedShort());
                // byte[0-1] 消息包总数(word(16))
                msgHeader.setTotalSubPackage(escape.readUnsignedShort());
                // byte[2-3] 包序号(word(16)) 从 1 开始
                msgHeader.setSubPackageSeq(escape.readUnsignedShort());
            }

            logger.info("得到消息头：{}", msgHeader);
            packageData.setMsgHeader(msgHeader);
            //消息体解析
            System.out.println("读指针： " + escape.readerIndex());
            System.out.println("写指针： " + escape.writerIndex());
            ByteBuf byteBuf = escape.readBytes(msgHeader.getMsgBodyLength());
            packageData.setPayload(byteBuf);
            System.out.println("消息体读指针： " + byteBuf.readerIndex());
            System.out.println("消息体写指针： " + byteBuf.writerIndex());
            //byte[] bytes = new byte[msgHeader.getMsgBodyLength()];
            //byteBuf.readBytes(bytes);
            //System.out.println("消息体:"+new String(bytes,"GBK"));

            packageData.setChannel(ctx.channel());
            this.processPackageData(packageData);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            //释放
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 处理数据逻辑
     * @param packageData
     */
    private void processPackageData(PackageData packageData) throws Exception {
        final MsgHeader header = packageData.getMsgHeader();
        switch (packageData.getMsgHeader().getMsgId()){
            //注册消息 256
            case TERNIMAL_MSG_REGISTER:
                logger.info(">>>>>[终端注册],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
                TerminalRegisterMsg terminalRegisterMsg = decoder.toTerminalRegisterMsg(packageData);
                terminalMsgProcessService.processRegisterMsg(terminalRegisterMsg);
                logger.info("<<<<<[终端注册],phone={},flowOd={}", header.getTerminalPhone(), header.getFlowId());
                break;
            //鉴权258
            case TERNIMAL_MSG_AUTH:
                logger.info(">>>>>[终端鉴权],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
                TerminalAuthenticationMsg terminalAuthenticationMsg = decoder.toTerminalAuthenticationMsg(packageData);
                terminalMsgProcessService.processAuthMsg(terminalAuthenticationMsg);
                logger.info("<<<<<[终端鉴权],phone={},flowOd={}", header.getTerminalPhone(), header.getFlowId());
                break;
            //位置512
            case TERNIMAL_MSG_LOCATION:
                logger.info(">>>>>[位置信息],phone={},flowid={}", header.getTerminalPhone(), header.getFlowId());
                LocationInfoUploadMsg locationInfoUploadMsg = decoder.toLocationInfoUploadMsg(packageData);
                terminalMsgProcessService.processlocationInfoUploadMsg(locationInfoUploadMsg);
                logger.info("<<<<<[位置信息],phone={},flowOd={}", header.getTerminalPhone(), header.getFlowId());
                break;
            default:

        }
    }


    /**
     * 根据byteBuf的readerIndex和writerIndex计算校验码
     * 校验码规则：从消息头开始，同后一字节异或，直到校验码前一个字节，占用 1 个字节
     *
     * @param byteBuf
     * @return
     */
    public static byte XorSumBytes(ByteBuf byteBuf) {
        byte sum = byteBuf.getByte(byteBuf.readerIndex());
        for (int i = byteBuf.readerIndex() + 1; i < byteBuf.writerIndex(); i++) {
            sum = (byte) (sum ^ byteBuf.getByte(i));
        }
        return sum;
    }

    /**
     * 读取n的第i位
     *
     * @param n
     * @param i
     * @return
     */
    public static int get(int n, int i) {
        return (1 << i) & n;
    }

    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(Arrays.toString("123".getBytes("GBK")));
    }
}
