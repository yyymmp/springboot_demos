package com.jt.server.codec;


import com.jt.server.message.PackageData;
import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JT808Decoder extends ByteToMessageDecoder {
    Logger log = LoggerFactory.getLogger(ByteToMessageDecoder.class);
    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        log.debug("<<<<< ip:{},hex:{}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(in));
        //转移
        ByteBuf unescape = unescape(in);
        log.info("转义后 " + ByteBufUtil.hexDump(unescape));
        log.info("可读数 " + unescape.readableBytes());

        //DataPacket msg = decode(in);
        //if (msg != null) {
        //    out.add(msg);
        //}
    }

//    private PackageData decode(ByteBuf in) {
//        if (in.readableBytes() < 12) { //包头最小长度
//            return null;
//        }
//        //转义
//        byte[] raw = new byte[in.readableBytes()];
//        in.readBytes(raw);
//        ByteBuf escape = revert(raw);
//        //校验
//        byte pkgCheckSum = escape.getByte(escape.writerIndex() - 1);
//        escape.writerIndex(escape.writerIndex() - 1);//排除校验码
//        byte calCheckSum = JT808Util.XorSumBytes(escape);
//        if (pkgCheckSum != calCheckSum) {
//            log.warn("校验码错误,pkgCheckSum:{},calCheckSum:{}", pkgCheckSum, calCheckSum);
//            ReferenceCountUtil.safeRelease(escape);
//            return null;
//        }
//        //解码
//        return parse(escape);
//    }

    /**
     * 将接收到的原始转义数据还原
     *
     * @param raw
     * @return
     */
    //public ByteBuf revert(byte[] raw) {
    //    int len = raw.length;
    //    ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer(len);//DataPacket parse方法回收
    //    for (int i = 0; i < len; i++) {
    //        //这里如果最后一位是0x7d会导致index溢出，说明原始报文转义有误
    //        if (raw[i] == 0x7d && raw[i + 1] == 0x01) {
    //            buf.writeByte(0x7d);
    //            i++;
    //        } else if (raw[i] == 0x7d && raw[i + 1] == 0x02) {
    //            buf.writeByte(0x7e);
    //            i++;
    //        } else {
    //            buf.writeByte(raw[i]);
    //        }
    //    }
    //    return buf;
    //}

    //public DataPacket parse(ByteBuf bb) {
    //    DataPacket packet = null;
    //    short msgId = bb.getShort(bb.readerIndex());
    //    switch (msgId) {
    //        case TERNIMAL_MSG_HEARTBEAT:
    //            packet = new HeartBeatMsg(bb);
    //            break;
    //        case TERNIMAL_MSG_LOCATION:
    //            packet = new LocationMsg(bb);
    //            break;
    //        case TERNIMAL_MSG_REGISTER:
    //            packet = new RegisterMsg(bb);
    //            break;
    //        case TERNIMAL_MSG_AUTH:
    //            packet = new AuthMsg(bb);
    //            break;
    //        case TERNIMAL_MSG_LOGOUT:
    //            packet = new LogOutMsg(bb);
    //            break;
    //        default:
    //            packet = new DataPacket(bb);
    //            break;
    //    }
    //    packet.parse();
    //    return packet;
    //}

    /**
     * 反转义  数据到达时会被转移
     * @param source
     * @return
     */
    public static ByteBuf unescape(ByteBuf source) {
        int low = source.readerIndex();
        int high = source.writerIndex();
        int last = high - 1;

        if (source.getByte(0) == 0x7e)
            low = low + 1;

        if (source.getByte(last) == 0x7e)
            high = last;

        int mark = source.indexOf(low, high, (byte) 0x7d);
        if (mark == -1) {
            return source.slice(low, high - low);
        }

        List<ByteBuf> bufList = new ArrayList<>(3);

        int len;
        do {

            len = mark + 2 - low;
            bufList.add(slice(source, low, len));
            low += len;

            mark = source.indexOf(low, high, (byte) 0x7d);
        } while (mark > 0);

        bufList.add(source.slice(low, high - low));

        return new CompositeByteBuf(ALLOC, false, bufList.size(), bufList);
    }

    /**
     * 截取转义前报文，并还原转义位
     */
    protected static ByteBuf slice(ByteBuf byteBuf, int index, int length) {
        byte second = byteBuf.getByte(index + length - 1);
        if (second == 0x02) {
            byteBuf.setByte(index + length - 2, 0x7e);
        }
        return byteBuf.slice(index, length - 1);
    }

}
