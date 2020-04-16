package com.shao.xfyun_tts.ting;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author shaohuailin
 * @date 2020/4/15 10:10
 */
public class Ting {
    private static Logger log = LoggerFactory.getLogger(Ting.class);

    private TingConfig config;
    private OutputStream os;

    public Ting(TingConfig config, OutputStream os){
        if (StringUtils.isEmpty(config.getHostUrl())
                || StringUtils.isEmpty(config.getAPPID())
                || StringUtils.isEmpty(config.getAPISecret())
                || StringUtils.isEmpty(config.getAPIKey())){
            throw new IllegalArgumentException("公共参数是必须的");
        }
        if (config.getBusiness() == null){
            throw new IllegalArgumentException("business是必须的");
        }
        this.config = config;
        this.os = os;
    }

    /**
     * 启动
     * @param text  合成的文本
     * @throws Exception
     */
    public void ignition(String text) throws Exception {
        //构建鉴权url
        String authUrl = Ting.getAuthUrl(config.getHostUrl(), config.getAPIKey(), config.getAPISecret());
        //将url中的 schema http://和https://分别替换为ws:// 和 wss://
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        this.connect(url, 80, text);
    }

    private void connect(String url, int port, String text) throws InterruptedException{
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            URI uri = new URI(url);
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ApplicationChannelHandler(url));

            ChannelFuture f = b.connect(uri.getHost(), port).sync();

            TingChannelHandler handler = (TingChannelHandler) f.channel().pipeline().get("Ting");
            //阻塞握手
            handler.handshakeFuture().sync();

            //发送请求数据
            TextWebSocketFrame frame = this.getReqFrame(text);
            f.channel().writeAndFlush(frame);

            f.channel().closeFuture().sync();

        } catch (URISyntaxException e) {
            log.error("无法将字符串解析为URL");
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private class ApplicationChannelHandler extends ChannelInitializer<SocketChannel> {

        private String url;

        public ApplicationChannelHandler(String url) {
            this.url = url;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            URI uri = new URI(url);
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

            ch.pipeline().addLast(new HttpClientCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(65536));
            ch.pipeline().addLast("Ting", new TingChannelHandler(handshaker, os));
        }
    }

    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        String authorization = String.format("hmac username=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);

        String httpUrl = "https://" + url.getHost() + url.getPath()
                + "?authorization=" + Base64.getEncoder().encodeToString(authorization.getBytes(charset))
                + "&date=" + URLEncoder.encode(date, "utf-8")
                + "&host=" + url.getHost();

        return httpUrl;
    }

    private TextWebSocketFrame getReqFrame(String text){
        //发送数据
        JSONObject frame = new JSONObject();
        JSONObject common = new JSONObject();
        JSONObject data = new JSONObject();
        // 填充common
        common.put("app_id", config.getAPPID());
        //填充business
        JSONObject business = config.getBusiness();
        //填充data
        data.put("status", 2);//固定位2
        try {
            data.put("text", Base64.getEncoder().encodeToString(text.getBytes("utf8")));
            //使用小语种须使用下面的代码，此处的unicode指的是 utf16小端的编码方式，即"UTF-16LE"”
            //data.addProperty("text", Base64.getEncoder().encodeToString(text.getBytes("UTF-16LE")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //填充frame
        frame.put("common", common);
        frame.put("business", business);
        frame.put("data", data);
        return new TextWebSocketFrame(frame.toJSONString());
    }

    public static void main(String[] args) throws Exception {

//        String hostUrl = "https://tts-api.xfyun.cn/v2/tts";
//        String APPID = "你的APPID";
//        String APISecret = "你的APISecret";
//        String APIKey = "你的APIKey";
//
//        JSONObject business = new JSONObject();
//        business.put("aue", "lame"); //lame：mp3 (当aue=lame时需传参sfl=1)
//        business.put("sfl", 1);
//        business.put("tte", "UTF8");//小语种必须使用UNICODE编码
//        business.put("ent", "intp65");
//        business.put("vcn", "x2_yezi");// 语音人
//        business.put("pitch", 50);
//        business.put("speed", 40);
//
//        TingConfig config = TingConfig.getInstance();
//        config.setCommon(hostUrl, APPID, APISecret, APIKey).setBusiness(business);
//
//        // 存放音频的文件
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//        String date = sdf.format(new Date());
//        File f = new File("D:/" + date + ".mp3");
//        if (!f.exists()) {
//            f.createNewFile();
//        }
//        FileOutputStream os = new FileOutputStream(f);
//
//
//        Ting ting = new Ting(config, os);
//        ting.ignition("告白气球");

    }
}
