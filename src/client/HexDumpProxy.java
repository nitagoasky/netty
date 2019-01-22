package client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class HexDumpProxy {

    //中间IP和端口
    private static final String PROXY_IP = "127.0.0.1";
    static final int PROXY_PORT = 9000;

    public static void main(String[] args) throws Exception
    {

        log.info("**********************启动代理 ********************** Ip:{} port:{}",PROXY_IP,PROXY_PORT);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try
        {   //这里启动代理服务器端,用来接收local端和remote端的消息
            //把从来自local端的消息转发到remote端
            //把从来自remote端的消息转发到local端
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HexDumpProxyInitializer())
                    .childOption(ChannelOption.AUTO_READ, false)
                    .bind(new InetSocketAddress(PROXY_IP, PROXY_PORT))  //代理端口
                    .sync().channel().closeFuture().sync();
        }
        finally
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
