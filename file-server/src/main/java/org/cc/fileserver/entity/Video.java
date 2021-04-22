package org.cc.fileserver.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ApiModel(value = "视频", description = "视频映射实体")
public class Video extends CacheFile {

    @ApiModelProperty("视频封面")
    private String coverUri;

    @ApiModelProperty("时长")
    private LocalTime totalTime;

}
