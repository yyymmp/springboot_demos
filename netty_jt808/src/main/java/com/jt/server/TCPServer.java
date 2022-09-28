package com.jt.server;

import com.jt.server.codec.JT808Decoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TCPServer {
    private final Logger log = Logger.getLogger(TCPServer.class.getName());

    private int port;

    public TCPServer(int port) {
        this.port = port;
    }

    public void bind() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)//
                .channel(NioServerSocketChannel.class) //
                .childHandler(new ChannelInitializer<SocketChannel>() { //
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast("idleStateHandler",
                                new IdleStateHandler(15, 0, 0, TimeUnit.MINUTES));
                        ch.pipeline().addLast(new LoggingHandler());
                        // 1024表示单条消息的最大长度，解码器在查找分隔符的时候，达到该长度还没找到的话会抛异常
                        ch.pipeline().addLast(
                                new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[] { 0x7e }),
                                        Unpooled.copiedBuffer(new byte[] { 0x7e, 0x7e })));
                         ch.pipeline().addLast(new JT808Decoder());
                        // ch.pipeline().addLast(new PackageDataDecoder());
                        //ch.pipeline().addLast(new TCPServerHandler());
                    }
                }).option(ChannelOption.SO_BACKLOG, 128) //
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        this.log.info("TCP服务启动完毕,port={}"+port);
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

        channelFuture.channel().closeFuture().sync();
    }

    public synchronized void startServer() {
        new Thread(() -> {
            try {
                this.bind();
            } catch (Exception e) {
                this.log.info("TCP服务启动出错");
                e.printStackTrace();
            }
        }, "TCPServer").start();
    }

    public static void main(String[] args) {
        new TCPServer(20049).startServer();
    }
}
