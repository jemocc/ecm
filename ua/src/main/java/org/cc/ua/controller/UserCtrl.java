package org.cc.ua.controller;

import org.cc.common.model.RspResult;
import org.cc.common.utils.JsonUtil;
import org.cc.common.model.User;
import org.cc.common.utils.PlatformUtil;
import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;
import org.cc.ua.server.UserRoleAndPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserCtrl {
    private final Logger log = LoggerFactory.getLogger(UserCtrl.class);

    @Resource
    private UserRoleAndPermissionService userRoleAndPermissionService;


    @GetMapping("/current-user")
    public RspResult<User> getCurrentUser() {
        User user = PlatformUtil.currentUser();
        log.info("用户：{}", JsonUtil.bean2Json_FN(user));
        return RspResult.ok(user);
    }

    @PostMapping("/save-role")
    public RspResult<Integer> saveRole(@RequestBody Role role) {
        log.info("save role, with data: {}", JsonUtil.bean2Json(role));
        int r = userRoleAndPermissionService.saveRole(role);
        return RspResult.ok(r);
    }

    @GetMapping("/get-roles")
    public RspResult<List<Role>> getRoles() {
        List<Role> roles = userRoleAndPermissionService.queryRoles();
        return RspResult.ok(roles);
    }

    @GetMapping("/get-permissions/{type}")
    public RspResult<List<Permission>> getPermissions(@PathVariable("type") String type) {
        User user = PlatformUtil.currentUser();
        List<Permission> permissions = userRoleAndPermissionService.getPermissions(user.getId(), Integer.parseInt(type));
        return RspResult.ok(permissions);
    }
}
