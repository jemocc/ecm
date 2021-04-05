package org.cc.ua.controller;

import com.alibaba.fastjson.JSONObject;
import org.cc.common.model.RspResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class AuthCtrl {

    Logger log = LoggerFactory.getLogger(AuthCtrl.class);

    @Resource
    private PasswordEncoder passwordEncoder;

    @GetMapping("/oauth/code")
    public RspResult<JSONObject> code(@RequestParam("code") String code) {
        JSONObject result = new JSONObject(1);
        result.put("code", code);
        return RspResult.ok(result);
    }

    @GetMapping(value = "/pr/password-encoder")
    public RspResult<JSONObject> passwordEncode(@RequestParam String password) {
        JSONObject result = new JSONObject(1);
        result.put("password", passwordEncoder.encode(password));
        return RspResult.ok(result);
    }
}
