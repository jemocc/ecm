package org.cc.ua.dao;

import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;

import java.util.List;

public interface UserRoleAndPermissionDao {

    int insertRole(Role role);
    int updateRole(Role role, boolean skipNull);

    Role queryRole(int id);
    List<Role> queryRoles(List<String> names);
    List<Role> queryChildRoles(String pSeqNo);
    List<Role> queryAllChildRoles(List<String> pSeqNos);


    int insertPermission(Permission p);
    int updatePermission(Permission p, boolean skipNull);
    List<Permission> queryPermission(int permissionType);
    List<Permission> queryPermission(int permissionType, List<Integer> roles);


}
