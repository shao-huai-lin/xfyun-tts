package com.shao.xfyun_tts.ting.data;

/**
 * 讯飞接口返回数据对象
 * @author shaohuailin
 * @date 2020/4/15 9:07
 */
public class ResponseData {
    private int code;
    private String message;
    private String sid;
    private Data data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
