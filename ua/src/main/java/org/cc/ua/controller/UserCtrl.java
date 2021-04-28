package org.cc.ua.controller;

import org.cc.common.model.RspResult;
import org.cc.common.utils.JsonUtil;
import org.cc.common.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserCtrl {
    private final Logger log = LoggerFactory.getLogger(UserCtrl.class);

    @GetMapping("/current-user")
    public RspResult<User> getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = (User) authentication.getPrincipal();
        user.setPassword(null);
        log.info("用户：{}", JsonUtil.bean2Json_FN(user));
        return RspResult.ok(user);
    }

}
