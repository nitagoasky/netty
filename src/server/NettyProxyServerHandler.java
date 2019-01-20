package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyProxyServerHandler extends ChannelInboundHandlerAdapter {

    private Channel channel;

    public NettyProxyServerHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(this.channel.isActive()){
            this.channel.writeAndFlush(o);
        }else {
            channelHandlerContext.close();
        }
    }
}
