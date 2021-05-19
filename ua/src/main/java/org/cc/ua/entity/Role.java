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
@ApiModel(value = "角色", description = "角色映射实体")
public class Role implements Serializable {

    @ApiModelProperty("角色ID")
    private Integer id;

    @ApiModelProperty("父角色ID")
    private Integer pid;

    @ApiModelProperty("角色顺序")
    private Integer sort;

    @ApiModelProperty("角色序号")
    private String seqNo;

    @ApiModelProperty("角色英文名称")
    private String name;

    @ApiModelProperty("角色中文描述")
    private String desc;

    @ApiModelProperty("角色状态 0-正常")
    private Integer status;

    @ApiModelProperty("备注1")
    private String remark1;

    @ApiModelProperty("子角色")
    private List<Role> childRole;
}
