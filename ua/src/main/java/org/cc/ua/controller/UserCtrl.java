package org.cc.ua.controller;

import org.cc.common.model.RspResult;
import org.cc.common.utils.JsonUtil;
import org.cc.common.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserCtrl {

    @GetMapping("/current-user")
    public RspResult<User> getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = JsonUtil.transfer(authentication.getPrincipal(), User.class);
        user.setPassword(null);
        return RspResult.ok(user);
    }

}
