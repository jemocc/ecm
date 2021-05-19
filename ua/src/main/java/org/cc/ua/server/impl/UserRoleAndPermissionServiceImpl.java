package org.cc.ua.server.impl;

import org.cc.common.exception.GlobalException;
import org.cc.common.model.User;
import org.cc.common.utils.PlatformUtil;
import org.cc.common.utils.PublicUtil;
import org.cc.ua.dao.UserRoleAndPermissionDao;
import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;
import org.cc.ua.exception.MissingOptPermission;
import org.cc.ua.server.UserRoleAndPermissionService;
import org.cc.ua.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        List<String> userRoleNames = Arrays.asList(currentUser.getRoles().split(","));
        if (r.getPid() == -1) {
            if (!userRoleNames.contains("ROLE_ADMIN"))
                throw new MissingOptPermission();
            else {
                r.setSeqNo(getNewSeqNo(""));
                return userRoleAndPermissionDao.insertRole(r);
            }
        } else {
            List<Role> userRoles = userRoleAndPermissionDao.queryRoles(userRoleNames);
            Role pRole = userRoleAndPermissionDao.queryRole(r.getPid());
            long f = userRoles.stream().filter(i -> pRole.getSeqNo().contains(i.getSeqNo())).count();
            if (f == 0)
                throw new MissingOptPermission();
            if (r.getId() == -1) {  //新增
                r.setSeqNo(getNewSeqNo(pRole.getSeqNo()));
                return userRoleAndPermissionDao.insertRole(r);
            } else {    //修改
                return userRoleAndPermissionDao.updateRole(r, false);
            }
        }
    }

    @Override
    public List<Role> queryRoles() {
        User user = PlatformUtil.currentUser();
        List<String> roleNames = Arrays.asList(user.getRoles().split(","));
        List<String> roleSeqNos = userRoleAndPermissionDao.queryRoles(roleNames).stream().map(Role::getSeqNo).collect(Collectors.toList());
        List<Role> roles = userRoleAndPermissionDao.queryAllChildRoles(roleSeqNos);
        return buildRoleTree(roles);
    }

    @Override
    public int savePermission(Permission p) {
        if (p.getId() == -1) {  //新增
            return userRoleAndPermissionDao.insertPermission(p);
        } else {    //修改
            return userRoleAndPermissionDao.updatePermission(p, false);
        }
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

    private String getNewSeqNo(String pSeqNo) {
        List<String> seqNos = userRoleAndPermissionDao.queryChildRoles(pSeqNo).stream().map(Role::getSeqNo).collect(Collectors.toList());
        if (seqNos.size() == Const.SORT_CHARS.length)
            throw new GlobalException(501, "同级角色数量已达上限");
        for (char c : Const.SORT_CHARS) {
            if (!seqNos.contains(pSeqNo + c))
                return pSeqNo + c;
        }
        return null;    //不会走到这里
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

    private List<Role> buildRoleTree(List<Role> roles) {
        if (roles.size() == 0)
            return new ArrayList<>(0);
        List<Role> result = new ArrayList<>();
        Map<Integer, Role> roleMap = new HashMap<>();
        roles.forEach(i -> {
            Role tr = roleMap.get(i.getPid());
            if (tr == null) {
                result.add(i);
            } else {
                List<Role> cr = tr.getChildRole();
                if (cr == null) {
                    cr = new ArrayList<>();
                    tr.setChildRole(cr);
                }
                cr.add(i.getSort(), i);
            }
            roleMap.put(i.getId(), i);
        });
        return result;
    }
}
