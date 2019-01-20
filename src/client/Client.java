package client;

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
import java.util.Arrays;

public class Client {
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
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                System.out.println(s);
                                channelHandlerContext.channel().writeAndFlush(s);
                            }
                        });
                    }
                });
        try {
            ChannelFuture channelFuture = null;
            channelFuture = bootstrap.bind(new InetSocketAddress(9000)).sync();
            ChannelFuture finalChannelFuture = channelFuture;
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if(finalChannelFuture.isSuccess()){
                    System.out.println("SUCCESS");
                } else {
                    System.out.println("ERROR");
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
