package org.cc.fileserver.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.cc.common.config.ExecutorConfig;
import org.cc.fileserver.entity.enums.FileFormType;

import java.time.LocalDateTime;
import java.time.LocalTime;

@ApiModel(value = "视频", description = "视频映射实体")
public class Video {

    @ApiModelProperty("视频ID")
    private Integer vid;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("文件类型")
    private String type;

    @ApiModelProperty("视频文件地址")
    private String vfUri;

    @ApiModelProperty("视频封面")
    private String vCover;

    @ApiModelProperty("时常")
    private LocalTime totalTime;

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

    public static Video ofNew(String name, String type, String vfUri, String vCover, FileFormType formType) {
        Video v = new Video();
        v.createAt = LocalDateTime.now();
        v.name = name;
        v.type = type;
        v.vfUri = vfUri;
        v.vCover = vCover;
        v.formType = formType;
        return v;
    }

    public void beginDown() { }

    public Integer getVid() {
        return vid;
    }

    public void setVid(Integer vid) {
        this.vid = vid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVfUri() {
        return vfUri;
    }

    public void setVfUri(String vfUri) {
        this.vfUri = vfUri;
    }

    public String getvCover() {
        return vCover;
    }

    public void setvCover(String vCover) {
        this.vCover = vCover;
    }

    public LocalTime getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(LocalTime totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupSort() {
        return groupSort;
    }

    public void setGroupSort(Integer groupSort) {
        this.groupSort = groupSort;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public FileFormType getFormType() {
        return formType;
    }

    public void setFormType(FileFormType formType) {
        this.formType = formType;
    }

    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remark1) {
        this.remark1 = remark1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remark2) {
        this.remark2 = remark2;
    }

    @Override
    public String toString() {
        return "Video{" +
                "vid=" + vid +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", vfUri='" + vfUri + '\'' +
                ", vCover='" + vCover + '\'' +
                ", totalTime=" + totalTime +
                ", groupId=" + groupId +
                ", groupSort=" + groupSort +
                ", createAt=" + createAt +
                ", formType=" + formType +
                ", remark1='" + remark1 + '\'' +
                ", remark2='" + remark2 + '\'' +
                '}';
    }
}
