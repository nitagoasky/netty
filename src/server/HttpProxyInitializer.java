package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyInitializer extends ChannelInboundHandlerAdapter {

    private Channel serverChannel;

    HttpProxyInitializer(Channel serverChannel) {
        this.serverChannel = serverChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (serverChannel != null) {
            log.info("向客户端写数据");
            serverChannel.writeAndFlush(msg);
        }
    }
}