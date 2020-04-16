package com.shao.xfyun_tts.conf;

import com.alibaba.fastjson.JSONObject;
import com.shao.xfyun_tts.ting.TingConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shaohuailin
 * @date 2020/4/15 22:45
 */
@Configuration
public class TingConfiguration {

    @Value("${xfyun.hostUrl}")
    private String hostUrl;
    @Value("${xfyun.APPID}")
    private String APPID;
    @Value("${xfyun.APISecret}")
    private String APISecret;
    @Value("${xfyun.APIKey}")
    private String APIKey;

    @Bean
    public TingConfig tingConfigBean(){

        JSONObject business = new JSONObject();
        business.put("aue", "lame"); //lame：mp3 (当aue=lame时需传参sfl=1)
        business.put("sfl", 1);
        business.put("tte", "UTF8");//小语种必须使用UNICODE编码
        business.put("ent", "intp65");
        business.put("vcn", "x2_yezi");// 语音人
        business.put("pitch", 50);
        business.put("speed", 40);

        TingConfig config = TingConfig.getInstance();
        config.setCommon(hostUrl, APPID, APISecret, APIKey).setBusiness(business);
        return config;
    }
}
