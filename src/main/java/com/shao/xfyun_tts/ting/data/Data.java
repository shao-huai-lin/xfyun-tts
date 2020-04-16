package com.shao.xfyun_tts.ting.data;

/**
 * @author shaohuailin
 * @date 2020/4/15 9:08
 */
public class Data {
    private int status;  //标志音频是否返回结束  status=1，表示后续还有音频返回，status=2表示所有的音频已经返回
    private String audio;  //返回的音频，base64 编码
    private String ced;  // 合成进度

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getCed() {
        return ced;
    }

    public void setCed(String ced) {
        this.ced = ced;
    }
}
