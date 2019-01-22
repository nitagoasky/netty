package client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel>
{

    public HexDumpProxyInitializer()
    {
    }
    @Override
    public void initChannel(SocketChannel ch)
    {
        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO),new HexDumpProxyFrontendHandler());
    }
}