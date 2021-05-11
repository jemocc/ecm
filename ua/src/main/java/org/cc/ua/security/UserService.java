package org.cc.ua.security;

import org.cc.common.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        String sql = "select * from users where u.username = ? and u.status = 0 limit 1";
        User user = null;
        try {
            user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), s);
            assert user != null;
            String sql2 = "select group_concat(name) from roles where find_set_in(id, ?)";
            String roleNames = jdbcTemplate.queryForObject(sql2, new Object[]{user.getRoleIds()}, String.class);
            user.setRoles(roleNames);
            return user;
        } catch (EmptyResultDataAccessException e) {
            if (user != null)
                throw new UsernameNotFoundException("用户角色异常");
            throw new UsernameNotFoundException("用户不存在: " + s);
        }
    }


}
