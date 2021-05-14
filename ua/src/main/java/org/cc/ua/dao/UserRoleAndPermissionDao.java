package org.cc.ua.dao;

import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;

import java.util.List;

public interface UserRoleAndPermissionDao {

    int insertRole(Role role);

    Role queryRole(int id);
    List<Role> queryRoles(List<String> names);



    List<Permission> queryPermission(int permissionType);

    List<Permission> queryPermission(int permissionType, List<Integer> roles);

}
