package org.cc.ua.controller;

import org.cc.ua.utils.VerifyCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class WebCtrl {

    Logger log = LoggerFactory.getLogger(WebCtrl.class);

    @GetMapping(value = "/verify-code-img.jpg")
    public void getVerifyCodeImg(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        ServletOutputStream sos = response.getOutputStream();
        String verifyCode = VerifyCodeUtil.outputVerifyImage(150,  50, sos, 4);
        request.getSession().setAttribute("verifyCode", verifyCode);
        log.info("Generate verify code: {}", verifyCode);
    }
}
