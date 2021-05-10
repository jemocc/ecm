package org.cc.ua.security;

import org.cc.common.model.User;
import org.cc.common.utils.PublicUtil;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        String sql = "select * from users where u.username = ? and u.status = 0 limit 1";
        try {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), s);
            assert user != null;
            List<Integer> roleIds = PublicUtil.splitStr(user.getRoles(), ",", Integer.class);
            String roleNames = jdbcTemplate.queryForObject("select group_concat(name) from roles where id in ?", new Object[]{roleIds}, String.class);
            user.setRoles(roleNames);
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("用户不存在: " + s);
        }
    }


}
