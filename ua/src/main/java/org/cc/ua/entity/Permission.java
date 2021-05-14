package org.cc.ua.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: Permission
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/14 10:52
 * @ModifyRecords: v1.0 new
 */
@Getter
@Setter
@ApiModel(value = "权限", description = "权限映射实体")
public class Permission implements Serializable {

    @ApiModelProperty("权限ID")
    private Integer id;

    @ApiModelProperty("权限父ID")
    private Integer pid;

    @ApiModelProperty("权限顺序")
    private Integer sort;

    @ApiModelProperty("权限英文名称")
    private String name;

    @ApiModelProperty("权限类型 0-菜单权限，1-功能权限")
    private Integer type;

    @ApiModelProperty("权限中文描述")
    private String desc;

    @ApiModelProperty("菜单路由")
    private String route;

    @ApiModelProperty("备注1")
    private String remark1;

    @ApiModelProperty("子权限")
    private List<Permission> childPermission;

    public Permission() {
    }

    public Permission(Integer id, List<Permission> childPermission) {
        this.id = id;
        this.childPermission = childPermission;
    }
}
