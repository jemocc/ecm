package org.cc.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

/**
 * create table ua.`users`(
 *   `uid` int(11) not null primary key auto_increment comment '用户ID',
 *   `username` varchar(50) not null comment '用户账号名称',
 *   `password` varchar(500) comment '用户密码',
 *   `status` int not null default 0 comment '用户状态 0-正常',
 *   `roles`   varchar(200) comment '用户角色，多角色以英文逗号分隔'
 * )ENGINE=InnoDB DEFAULT CHARSET=utf8;
 * ALTER TABLE ua.users ADD UNIQUE INDEX (username);
 */
@ApiModel(value = "用户", description = "系统基础用户映射实体")
public class User implements UserDetails, Serializable {

    @ApiModelProperty("用户ID")
    private Integer id;
    @ApiModelProperty("用户账号名称")
    private String username;
    @ApiModelProperty("用户密码")
    private String password;
    @ApiModelProperty("用户状态 0-正常")
    private Integer status = 0;
    @ApiModelProperty("用户角色，多角色以英文逗号分隔")
    private String roles;

    @Override
    public boolean isAccountNonExpired() {
        return status == 0;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == 0;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return status == 0;
    }

    @Override
    public boolean isEnabled() {
        return status == 0;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(this.roles);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public Integer getUid() {
        return id;
    }

    public void setUid(Integer uid) {
        this.id = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", status=" + status +
                ", roles='" + roles + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
