package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


public class Server {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(2);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))

                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 发送缓冲器
                    .option(ChannelOption.SO_SNDBUF, 1024)
                    // 接收缓冲器
                    .option(ChannelOption.SO_RCVBUF, 1024)
                    // 接收缓冲分配器
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(256, 2048, 65536))
                    // work线程参数设置
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(256, 2048, 65536))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast("httpCodec", new HttpServerCodec());
                            ch.pipeline().addLast("httpObject", new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("serverHandle", new HttpProxyServerHandle());
                        }
                    });
            ChannelFuture f = b.bind(8080).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
