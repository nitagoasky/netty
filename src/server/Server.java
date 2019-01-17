package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args){
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new StringDecoder());
                        channel.pipeline().addLast(new StringEncoder());
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            private ChannelHandlerContext innerCtx;
                            ChannelFuture connectFuture;
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                innerCtx = ctx;
                                Bootstrap bootstrap = new Bootstrap();
                                bootstrap.group(ctx.channel().eventLoop());
                                bootstrap.channel(NioSocketChannel.class)
                                        .handler(new SimpleChannelInboundHandler<ByteBuf>() {
                                            @Override
                                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                                System.out.println("客户端连接服务");
                                            }

                                            @Override
                                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                                byte[] dst = new byte[byteBuf.readableBytes()];
                                                byteBuf.readBytes(dst);
                                                System.out.println(dst);
                                            }
                                        });
                                connectFuture = bootstrap.connect(new InetSocketAddress(8888));
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                if (connectFuture.isDone()) {
                                    if (innerCtx != null && innerCtx.channel().isActive()) {
                                        innerCtx.writeAndFlush(s);
                                    }
                                }
                            }
                        });
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(8080));
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if(channelFuture.isSuccess()){
                System.out.println("SUCCESS");
            } else {
                System.out.println("ERROR");
            }
        });
    }

}
