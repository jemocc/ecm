package org.cc.ua.dao.impl;

import org.cc.common.utils.DBUtil;
import org.cc.ua.dao.UserRoleAndPermissionDao;
import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: UserRoleAndPermissionDaoImpl
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/14 11:00
 * @ModifyRecords: v1.0 new
 */
@Repository
public class UserRoleAndPermissionDaoImpl implements UserRoleAndPermissionDao {
    private final JdbcTemplate jdbcTemplate;

    public UserRoleAndPermissionDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int insertRole(Role role) {
        final String sql = "insert into roles (pid, sort, seq_no, name, desc, status, remark1) values (?,?,?,?,?,?,?)";
        final Object[] args = new Object[]{
                role.getPid(), role.getSeqNo(), role.getName(), role.getDesc(), role.getStatus(), role.getRemark1()
        };
        return DBUtil.insertRId(jdbcTemplate, sql, args);
    }

    @Override
    public Role queryRole(int id) {
        String sql = "select * from roles where id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Role.class));
    }

    @Override
    public List<Role> queryRoles(List<String> names) {
        String sql = "select * from roles where name in ?";
        return jdbcTemplate.query(sql, new Object[]{names}, new BeanPropertyRowMapper<>(Role.class));
    }

    @Override
    public List<Permission> queryPermission(int permissionType) {
        String sql = "select * from permissions where type = ? order by sort";
        return jdbcTemplate.query(sql, new Object[]{permissionType}, new BeanPropertyRowMapper<>(Permission.class));
    }

    @Override
    public List<Permission> queryPermission(int permissionType, List<Integer> roles) {
        String sql = "select p.* from role_to_permission rtp left join permissions p on p.id = rtp.pid where type = ? and rtp.rid in ?";
        return jdbcTemplate.query(sql, new Object[]{permissionType, roles}, new BeanPropertyRowMapper<>(Permission.class));
    }
}
