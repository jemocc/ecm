package org.cc.ua.dao.impl;

import org.cc.common.component.DistributeSynchronized;
import org.cc.common.utils.DBUtil;
import org.cc.ua.dao.UserRoleAndPermissionDao;
import org.cc.ua.entity.Permission;
import org.cc.ua.entity.Role;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @DistributeSynchronized("ADD_ROLE")
    public int insertRole(Role role) {
        final String sql = "insert into roles (pid, sort, seq_no, name, desc, status, remark1) values (?,?,?,?,?,?,?)";
        return DBUtil.insertRId(jdbcTemplate, sql, new Object[]{
                role.getPid(), role.getSeqNo(), role.getName(), role.getDesc(), role.getStatus(), role.getRemark1()
        });
    }

    @Override
    public int updateRole(Role role, boolean skipNull) {
        final String sql = "update roles set where id=?";
        Map<String, Object> us = new HashMap<>();
        us.put("sort", role.getSort());
        us.put("name", role.getName());
        us.put("desc", role.getDesc());
        us.put("status", role.getStatus());
        us.put("remark1", role.getRemark1());
        return DBUtil.update(jdbcTemplate, sql, us, skipNull, new Object[]{ role.getId() });
    }

    @Override
    public Role queryRole(int id) {
        final String sql = "select * from roles where id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id}, new BeanPropertyRowMapper<>(Role.class));
    }

    @Override
    public List<Role> queryRoles(List<String> names) {
        final String sql = "select * from roles where name in ?";
        return jdbcTemplate.query(sql, new Object[]{names}, new BeanPropertyRowMapper<>(Role.class));
    }

    @Override
    public List<Role> queryChildRoles(String pSeqNo) {
        final String sql = "select * from roles where seq_no > ? and seq_no < ?";
        Object[] args = new Object[]{pSeqNo, pSeqNo + "z0"};
        return jdbcTemplate.query(sql, args, new BeanPropertyRowMapper<>(Role.class));
    }

    @Override
    public List<Role> queryAllChildRoles(List<String> pSeqNos) {
        StringBuilder sb = new StringBuilder("select * from roles where");
        for (int i = 0; i < pSeqNos.size(); i++) {
            String seqNo = pSeqNos.get(i);
            char c = (char) (seqNo.charAt(seqNo.length() - 1) + 1);
            sb.append(" (seq_no > '").append(seqNo).append("' and seq_no < '").append(seqNo.replaceAll(".$", "" + c)).append("') or");
        }
        sb.delete(sb.length() - 2, sb.length()).append("order by seq_no");
        return jdbcTemplate.query(sb.toString(), new BeanPropertyRowMapper<>(Role.class));
    }

    @Override
    public int insertPermission(Permission p) {
        final String sql = "insert into permissions (pid, sort, name, type, desc, route, remark1) values (?,?,?,?,?,?,?)";
        return DBUtil.insertRId(jdbcTemplate, sql, new Object[]{
                p.getPid(), p.getSort(), p.getName(), p.getType(), p.getDesc(), p.getRoute(), p.getRemark1()
        });
    }

    @Override
    public int updatePermission(Permission p, boolean skipNull) {
        final String sql = "update permissions set where id=?";
        Map<String, Object> us = new HashMap<>();
        us.put("sort", p.getSort());
        us.put("name", p.getName());
        us.put("desc", p.getDesc());
        us.put("route", p.getRoute());
        us.put("remark1", p.getRemark1());
        return DBUtil.update(jdbcTemplate, sql, us, skipNull, new Object[]{ p.getId() });
    }


    @Override
    public List<Permission> queryPermission(int permissionType) {
        final String sql = "select * from permissions where type = ? order by sort";
        return jdbcTemplate.query(sql, new Object[]{permissionType}, new BeanPropertyRowMapper<>(Permission.class));
    }

    @Override
    public List<Permission> queryPermission(int permissionType, List<Integer> roles) {
        final String sql = "select p.* from role_to_permission rtp left join permissions p on p.id = rtp.pid where type = ? and rtp.rid in ?";
        return jdbcTemplate.query(sql, new Object[]{permissionType, roles}, new BeanPropertyRowMapper<>(Permission.class));
    }
}
