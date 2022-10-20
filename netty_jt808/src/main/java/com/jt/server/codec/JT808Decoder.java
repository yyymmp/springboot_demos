package com.jt.server.codec;


import com.jt.server.consts.JT808Const;
import com.jt.server.message.PackageData;
import com.jt.server.message.req.LocationInfoUploadMsg;
import com.jt.server.message.req.TerminalAuthenticationMsg;
import com.jt.server.message.req.TerminalRegisterMsg;
import com.jt.server.message.req.TerminalRegisterMsg.TerminalRegInfo;
import com.jt.server.uitls.BCD8421Operater;
import com.jt.server.uitls.BitOperator;
import io.netty.buffer.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JT808Decoder extends ByteToMessageDecoder {
    Logger log = LoggerFactory.getLogger(ByteToMessageDecoder.class);
    private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;
    private final BitOperator bitOperator = new BitOperator();
    private final BCD8421Operater bcd8421Operater = new BCD8421Operater();

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

    public TerminalRegisterMsg toTerminalRegisterMsg(PackageData packageData) {
        TerminalRegisterMsg ret = new TerminalRegisterMsg(packageData);
        TerminalRegInfo body = new TerminalRegInfo();

        //省id 两字节
        int provinceId = packageData.getPayload().readUnsignedShort();
        body.setProvinceId(provinceId);
        //市id 两字节
        int cityId = packageData.getPayload().readUnsignedShort();
        body.setCityId(cityId);
        //制造商id 5字节
        byte[] manufacturerIdBytes = new byte[5];
        packageData.getPayload().readBytes(manufacturerIdBytes);
        String manufacturerIdStr = new String(manufacturerIdBytes);
        body.setManufacturerId(manufacturerIdStr);
        //终端型号 20字节
        byte[] terminalTypeByte = new byte[20];
        packageData.getPayload().readBytes(terminalTypeByte);
        String terminalTypeByteStr = new String(terminalTypeByte);
        body.setTerminalType(terminalTypeByteStr);
        //终端id 7个字节
        byte[] terminalIdBytes = new byte[7];
        packageData.getPayload().readBytes(terminalIdBytes);
        String terminalIdStr = new String(terminalIdBytes);
        body.setTerminalId(terminalIdStr);
        //车身颜色 一个字节
        short licensePlateColor = packageData.getPayload().readUnsignedByte();
        body.setLicensePlateColor(licensePlateColor);
        //车辆标识(车牌号) 剩余字节
        byte[] licensePlateBytes = new byte[ret.getPayload().readableBytes()];
        packageData.getPayload().readBytes(licensePlateBytes);
        String licensePlateBytesStr = new String(licensePlateBytes);
        body.setLicensePlate(licensePlateBytesStr);

        ret.setTerminalRegInfo(body);
        log.info("解得注册消息体:{}",body);
        return ret;
        //byte[] data = new byte[ret.getPayload().readableBytes()];
        //packageData.getPayload().readBytes(data);
        //
        //TerminalRegInfo body = new TerminalRegInfo();
        //
        //// 1. byte[0-1] 省域ID(WORD)
        //// 设备安装车辆所在的省域，省域ID采用GB/T2260中规定的行政区划代码6位中前两位
        //// 0保留，由平台取默认值
        //body.setProvinceId(this.parseIntFromBytes(data, 0, 2));
        //
        //// 2. byte[2-3] 设备安装车辆所在的市域或县域,市县域ID采用GB/T2260中规定的行 政区划代码6位中后四位
        //// 0保留，由平台取默认值
        //body.setCityId(this.parseIntFromBytes(data, 2, 2));
        //
        //// 3. byte[4-8] 制造商ID(BYTE[5]) 5 个字节，终端制造商编码
        //// byte[] tmp = new byte[5];
        //body.setManufacturerId(this.parseStringFromBytes(data, 4, 5));
        //
        //// 4. byte[9-16] 终端型号(BYTE[8]) 八个字节， 此终端型号 由制造商自行定义 位数不足八位的，补空格。
        //body.setTerminalType(this.parseStringFromBytes(data, 9, 8));
        //
        //// 5. byte[17-23] 终端ID(BYTE[7]) 七个字节， 由大写字母 和数字组成， 此终端 ID由制造 商自行定义
        //body.setTerminalId(this.parseStringFromBytes(data, 17, 7));
        //
        //// 6. byte[24] 车牌颜色(BYTE) 车牌颜 色按照JT/T415-2006 中5.4.12 的规定
        //body.setLicensePlateColor(this.parseIntFromBytes(data, 24, 1));
        //
        //// 7. byte[25-x] 车牌(STRING) 公安交 通管理部门颁 发的机动车号牌
        //body.setLicensePlate(this.parseStringFromBytes(data, 25, data.length - 25));
        //
        //ret.setTerminalRegInfo(body);
        //return ret;
    }


    public TerminalAuthenticationMsg toTerminalAuthenticationMsg(PackageData packageData) throws UnsupportedEncodingException {
        return new TerminalAuthenticationMsg(packageData);
    }


    private int parseIntFromBytes(byte[] data, int startIndex, int length) {
        return this.parseIntFromBytes(data, startIndex, length, 0);
    }

    private int parseIntFromBytes(byte[] data, int startIndex, int length, int defaultVal) {
        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 4 ? 4 : length;
            byte[] tmp = new byte[len];
            System.arraycopy(data, startIndex, tmp, 0, len);
            return bitOperator.byteToInteger(tmp);
        } catch (Exception e) {
            log.error("解析整数出错:{}", e.getMessage());
            e.printStackTrace();
            return defaultVal;
        }
    }

    protected String parseStringFromBytes(byte[] data, int startIndex, int lenth) {
        return this.parseStringFromBytes(data, startIndex, lenth, null);
    }

    private String parseStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
        try {
            byte[] tmp = new byte[lenth];
            System.arraycopy(data, startIndex, tmp, 0, lenth);
            return new String(tmp,"GBK");
        } catch (Exception e) {
            log.error("解析字符串出错:{}", e.getMessage());
            e.printStackTrace();
            return defaultVal;
        }
    }

    /**
     * 转义待发数据  0x7e
     * @param raw
     * @return
     */
    public static ByteBuf escape(ByteBuf raw) {
        int len = raw.readableBytes();
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(len + 12);
        buf.writeByte(JT808Const.PKG_DELIMITER);
        while (len > 0) {
            byte b = raw.readByte();
            if (b == 0x7e) {
                buf.writeByte(0x7d);
                buf.writeByte(0x02);
            } else if (b == 0x7d) {
                buf.writeByte(0x7d);
                buf.writeByte(0x01);
            } else {
                buf.writeByte(b);
            }
            len--;
        }
        ReferenceCountUtil.safeRelease(raw);
        buf.writeByte(JT808Const.PKG_DELIMITER);
        return buf;
    }

    public LocationInfoUploadMsg toLocationInfoUploadMsg(PackageData packageData) {
        LocationInfoUploadMsg locationInfoUploadMsg = new LocationInfoUploadMsg(packageData);
        ByteBuf payload = packageData.getPayload();
        //报警标志 4字节
        int warningFlagField = payload.readInt();
        locationInfoUploadMsg.setWarningFlagField(warningFlagField);
        //状态 4字节
        int statusField = payload.readInt();
        locationInfoUploadMsg.setStatusField(statusField);
        //纬度 4字节
        float latitude = payload.readInt() *1.0F/100_0000;
        locationInfoUploadMsg.setLatitude(latitude);
        //经度 4字节
        float longitude = payload.readInt() *1.0F/100_0000;
        locationInfoUploadMsg.setLongitude(longitude);
        //高程 2字节
        int elevation = payload.readUnsignedShort();
        locationInfoUploadMsg.setElevation(elevation);
        //速度 2字节
        int speed = payload.readUnsignedShort();
        locationInfoUploadMsg.setSpeed(speed);
        //方向 2字节
        int direction = payload.readUnsignedShort();
        locationInfoUploadMsg.setDirection(direction);
        //时间 6字节 这是bcd码
        byte[] bs = new byte[6];
        payload.readBytes(bs);
        String dateStr = BCD8421Operater.bcd2String(bs);
        Date date = null;
        try {
            date = new SimpleDateFormat("yyMMddHHmmss").parse(dateStr);
        } catch (ParseException e) {
            log.error("",e);
        }
        locationInfoUploadMsg.setTime(date);

        return locationInfoUploadMsg;
    }


}
