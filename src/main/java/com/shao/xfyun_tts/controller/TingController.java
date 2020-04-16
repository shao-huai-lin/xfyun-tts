package com.shao.xfyun_tts.controller;

import com.shao.xfyun_tts.ting.Ting;
import com.shao.xfyun_tts.ting.TingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author shaohuailin
 * @date 2020/4/16 18:09
 */
@Controller
@RequestMapping("/ting")
public class TingController {

    @Autowired
    private TingConfig config;

    @GetMapping("/audio")
    @ResponseBody
    public void audio(String text, HttpServletResponse response){
        if (StringUtils.isEmpty(text)){
            response.setStatus(400);
            return;
        }

        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("audio/mp3");
        response.setStatus(200);
        try {
            ServletOutputStream os = response.getOutputStream();
            Ting ting = new Ting(config, os);
            ting.ignition(text);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
