package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()) // 注册线程池
                    .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new HttpProxyInitializer(ctx.channel()));

            ChannelFuture cf = bootstrap.connect("127.0.0.1", 8888);
            cf.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(msg);
                } else {
                    ctx.channel().close();
                }
            });
        }
    }


}