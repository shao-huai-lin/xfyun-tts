package com.shao.xfyun_tts.ting;

import com.alibaba.fastjson.JSONObject;

/**
 * 配置文件
 * @author shaohuailin
 * @date 2020/4/15 21:28
 */
public class TingConfig {

    private String hostUrl;
    private String APPID;
    private String APISecret;
    private String APIKey;

    private JSONObject business;

    private static TingConfig config;

    private TingConfig(){}

    public static TingConfig getInstance() {
        if (config == null){
            config = new TingConfig();
        }
        return config;
    }

    /**
     * 设置公共参数
     * @param hostUrl
     * @param APPID
     * @param APISecret
     * @param APIKey
     * @return
     */
    public TingConfig setCommon(String hostUrl, String APPID, String APISecret, String APIKey){
        this.hostUrl = hostUrl;
        this.APPID = APPID;
        this.APISecret = APISecret;
        this.APIKey = APIKey;
        return this;
    }

    /**
     * 设置business参数
     * @param business
     * @return
     */
    public TingConfig setBusiness(JSONObject business){
        this.business = business;
        return this;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public String getAPPID() {
        return APPID;
    }

    public String getAPISecret() {
        return APISecret;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public JSONObject getBusiness() {
        return business;
    }
}
