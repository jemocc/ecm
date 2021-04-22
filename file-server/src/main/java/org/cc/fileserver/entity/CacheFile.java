package org.cc.fileserver.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.cc.common.utils.JsonUtil;
import org.cc.fileserver.entity.enums.FileFormType;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel(value = "文件资源", description = "文件映射实体")
public class CacheFile {

    @ApiModelProperty("文件ID")
    private Integer id;

    @ApiModelProperty("文件名称")
    private String name;

    @ApiModelProperty("文件类型")
    private String type;

    @ApiModelProperty("文件资源位置")
    private String uri;

    @ApiModelProperty("分组")
    private Integer groupId;

    @ApiModelProperty("组内序号")
    private Integer groupSort;

    @ApiModelProperty("创建时间")
    private LocalDateTime createAt;

    @ApiModelProperty("来源类型")
    private FileFormType formType;

    @ApiModelProperty("备注1")
    private String remark1;

    @ApiModelProperty("备注2")
    private String remark2;

    @Override
    public String toString() {
        return JsonUtil.bean2Json_FN(this);
    }
}
