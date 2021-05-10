package org.cc.ua.controller;

import com.google.gson.JsonObject;
import org.cc.common.model.RspResult;
import org.cc.ua.utils.VerifyCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
public class AuthCtrl {

    Logger log = LoggerFactory.getLogger(AuthCtrl.class);
    @Resource
    private PasswordEncoder passwordEncoder;

    @GetMapping("/oauth/code")
    public RspResult<JsonObject> code(@RequestParam("code") String code) {
        JsonObject result = new JsonObject();
        result.addProperty("code", code);
        return RspResult.ok(result);
    }

    @GetMapping("/verify-code-img.jpg")
    public void getVerifyCodeImg(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        ServletOutputStream sos = response.getOutputStream();
        HttpSession session = request.getSession();
        String verifyCode = VerifyCodeUtil.outputVerifyImage(150,  50, sos, 4, session);
        log.info("Generate verify code: {}", verifyCode);
    }

    @GetMapping("/pr/password-encoder")
    @PreAuthorize("hasRole('ADMIN')")
    public RspResult<JsonObject> passwordEncode(@RequestParam String password) {
        JsonObject result = new JsonObject();
        result.addProperty("password", passwordEncoder.encode(password));
        return RspResult.ok(result);
    }
}
