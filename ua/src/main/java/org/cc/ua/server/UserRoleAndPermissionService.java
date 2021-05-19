package org.cc.ua.server;

import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;

import java.util.List;

/**
 * @ClassName: userRoleAndPermissionService
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/14 10:48
 * @ModifyRecords: v1.0 new
 */
public interface UserRoleAndPermissionService {

    int saveRole(Role r);

    List<Role> queryRoles();

    int savePermission(Permission p);

    List<Permission> getPermissions(int userId, int permissionType);

}
