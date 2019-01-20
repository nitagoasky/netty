package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Arrays;

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
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                            ChannelFuture connectFuture = null;

                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                    if(connectFuture != null && connectFuture.channel().isActive()){
                                        connectFuture.channel().writeAndFlush(o);
                                    } else {
                                        Bootstrap b = new Bootstrap();
                                        b.group(channelHandlerContext.channel().eventLoop())
                                                .channel(NioSocketChannel.class)
                                                .handler(new ChannelInitializer<Channel>(){
                                                    @Override
                                                    public void initChannel(Channel ch) throws Exception {
                                                        ChannelPipeline p = ch.pipeline();
                                                        p.addLast(new HttpRequestDecoder());
                                                        p.addLast(new HttpResponseEncoder());
//                                                        p.addLast(new HttpObjectAggregator(1048576));
                                                        p.addLast(new NettyProxyServerHandler(channelHandlerContext.channel()));
                                                    }
                                                });
                                        connectFuture = b.connect("127.0.0.1", 8888);
                                        connectFuture.addListener((ChannelFutureListener) channelFuture -> {
                                            if(channelFuture.isSuccess()){
                                                channelFuture.channel().writeAndFlush(o);
                                            }
                                        });
                                    }
                            }
                        });
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(new InetSocketAddress(8080));
        channelFuture.channel().closeFuture();
        channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            if(channelFuture.isSuccess()){
                System.out.println("SUCCESS");
            } else {
                System.out.println("ERROR");
            }
        });
    }

}
