package org.cc.ua.server.impl;

import org.cc.common.model.User;
import org.cc.common.utils.PlatformUtil;
import org.cc.common.utils.PublicUtil;
import org.cc.ua.dao.UserRoleAndPermissionDao;
import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;
import org.cc.ua.exception.MissingOptPermission;
import org.cc.ua.server.UserRoleAndPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName: userRoleAndPermissionServiceImpl
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/14 11:01
 * @ModifyRecords: v1.0 new
 */
@Service
public class UserRoleAndPermissionServiceImpl implements UserRoleAndPermissionService {
    private final Logger log = LoggerFactory.getLogger(UserRoleAndPermissionServiceImpl.class);

    private final UserRoleAndPermissionDao userRoleAndPermissionDao;

    public UserRoleAndPermissionServiceImpl(UserRoleAndPermissionDao userRoleAndPermissionDao) {
        this.userRoleAndPermissionDao = userRoleAndPermissionDao;
    }


    @Override
    public int saveRole(Role r) {
        //检查是否有操作目标数据的权限
        User currentUser = PlatformUtil.currentUser();
        List<Role> userRoles = userRoleAndPermissionDao.queryRoles(Arrays.asList(currentUser.getRoles().split(",")));
        Role pRole = userRoleAndPermissionDao.queryRole(r.getPid());
        long f = userRoles.stream().filter(i -> pRole.getSeqNo().contains(i.getSeqNo())).count();
        if (f == 0)
            throw new MissingOptPermission();
        if (r.getId() == 0) {

        } else {

        }
        return 0;
    }

    @Override
    public List<Role> queryRoles() {
        User user = PlatformUtil.currentUser();
        List<String> roleNames = Arrays.asList(user.getRoles().split(","));
        List<Role> roles = userRoleAndPermissionDao.queryRoles(roleNames);
        return null;
    }

    @Override
    public List<Permission> getPermissions(int userId, int permissionType) {
        //查询用户角色
        String roleIdsStr = "0,1";
        List<Integer> roleIds = PublicUtil.splitStr(roleIdsStr, ",", Integer.class);
        //查询用户权限
        List<Permission> permissions;
        if (roleIds.contains(0)) {
            permissions = userRoleAndPermissionDao.queryPermission(permissionType);
        } else {
            permissions = userRoleAndPermissionDao.queryPermission(permissionType, roleIds);
        }
        //拼装权限树
        return buildPermissionTree(permissions);
    }

    private List<Permission> buildPermissionTree(List<Permission> permissions) {
        if (permissions.size() == 0)
            return new ArrayList<>(0);
        List<Permission> result = new ArrayList<>();
        Map<Integer, Permission> permissionMap = new HashMap<>();
        permissions.forEach(i -> {
            Permission tp = permissionMap.get(i.getId());
            if (tp != null)
                i.setChildPermission(tp.getChildPermission());
            if (i.getPid() == -1)
                result.add(i);
            else {
                Permission tpp = permissionMap.computeIfAbsent(i.getPid(), j -> new Permission(i.getPid(), new ArrayList<>()));
                tpp.getChildPermission().add(i);
            }
            permissionMap.put(i.getId(), i);
        });
        return result;
    }
}
