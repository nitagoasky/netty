package client;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class HexDumpProxyBackendHandler extends ChannelInboundHandlerAdapter {

    //写入本地的通道
    private volatile Channel outbound2LocalChannel;


    public HexDumpProxyBackendHandler(Channel outbound2LocalChannel)
    {
        this.outbound2LocalChannel =outbound2LocalChannel;
    }
    //当proxy与local或者remote连接时,开始从proxy中读取数据
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        InetSocketAddress address = (InetSocketAddress)inboundChannel.remoteAddress();
        log.info("##################代理连接目标端口成功#######################");
        log.info("连接目标端口成功。 ip:{} port:{}",address.getHostName(),address.getPort());
        ctx.read();

    }
    //把proxy中的数据读取,同时把数据写入local或者remote端
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        log.info("##################目标服务器向代理写入数据#######################");
        InetSocketAddress fromAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        log.debug("数据来自:{}",fromAddress.getHostName());
        outbound2LocalChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                InetSocketAddress toAddress = (InetSocketAddress)outbound2LocalChannel.remoteAddress();
                log.debug("数据发往:{}",toAddress.getHostName());
            } else {
                future.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("############代理和目标地址端口断开连接##############");
        HexDumpProxyFrontendHandler.closeOnFlush(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info("############代理和目标地址端口断开连接##############");
        log.debug("exceptionCaught:{}",cause.getMessage());
        cause.printStackTrace();
        HexDumpProxyFrontendHandler.closeOnFlush(ctx.channel());
    }
}