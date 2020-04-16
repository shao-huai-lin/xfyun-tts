package com.shao.xfyun_tts.ting;

import com.alibaba.fastjson.JSONObject;
import com.shao.xfyun_tts.ting.data.ResponseData;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

/**
 * @author shaohuailin
 * @date 2020/4/9 19:09
 */
public class TingChannelHandler extends SimpleChannelInboundHandler<Object> {

    private static Logger log = LoggerFactory.getLogger(TingChannelHandler.class);

    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private OutputStream os;

    public TingChannelHandler(WebSocketClientHandshaker handshaker, OutputStream os) {
        this.handshaker = handshaker;
        this.os = os;
    }

    public ChannelPromise handshakeFuture(){
        return this.handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //建立连接
        //握手
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //连接关闭
        log.info("WebSocket Client received closing");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if ( ! handshaker.isHandshakeComplete()){
            handshaker.finishHandshake(channel, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status()
                    + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

            boolean b = this.texthandler(textFrame.text());
            if (b){
                channel.close();
            }
        } else if (frame instanceof CloseWebSocketFrame) {
            channel.close();
        }

    }

    private boolean texthandler(String text){
        ResponseData resp = JSONObject.parseObject(text, ResponseData.class);
        if (resp != null) {
            if (resp.getCode() != 0) {
                log.error(resp.getMessage() + " sid=" + resp.getSid());
                return true;
            }

            if (resp.getData() != null) {
                String result = resp.getData().getAudio();
                byte[] audio = Base64.getDecoder().decode(result);
                try {
                    os.write(audio);
                    os.flush();
                } catch (IOException e) {
                    //忽略中断异常
                }
                if (resp.getData().getStatus() == 2) {
                    // todo  resp.data.status ==2 说明数据全部返回完毕，可以关闭连接，释放资源
                    log.info("Session End");
                    try {
                        os.close();
                    } catch (IOException e) {
                        //忽略中断异常
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
