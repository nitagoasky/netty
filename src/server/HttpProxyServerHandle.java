package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        log.info("客户端连入 " + ctx.channel().remoteAddress());

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop()) // 注册线程池
                .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                .handler(new HttpProxyInitializer(ctx.channel()));

        ChannelFuture cf = bootstrap.connect("127.0.0.1", 8888);
        cf.addListener((ChannelFutureListener) future -> {
            log.info("连接服务端成功");
            if (future.isSuccess()) {
                log.info("像服务端开始写数据");
                future.channel().writeAndFlush(msg);
            } else {
                log.info("关闭服务端");
                ctx.channel().close();
            }
        });

    }


}