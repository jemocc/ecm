package org.cc.fileserver.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.cc.fileserver.entity.enums.FileFormType;

import java.time.LocalDateTime;

@ApiModel(value = "文件资源", description = "文件映射实体")
public class CacheFile {

    @ApiModelProperty("文件ID")
    private Integer fid;

    @ApiModelProperty("文件名称")
    private String name;

    @ApiModelProperty("文件类型")
    private String type;

    @ApiModelProperty("文件资源位置")
    private String uri;

    @ApiModelProperty("创建时间")
    private LocalDateTime createAt;

    @ApiModelProperty("来源类型")
    private FileFormType formType;

    @ApiModelProperty("备注1")
    private String remark1;

    @ApiModelProperty("备注2")
    private String remark2;

    public static CacheFile ofNew(String name, String type, String uri, FileFormType formType) {
        CacheFile file = new CacheFile();
        file.createAt = LocalDateTime.now();
        file.name = name;
        file.type = type;
        file.uri = uri;
        file.formType = formType;
        return file;
    }

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
        return "File{" +
                "fid=" + fid +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", uri='" + uri + '\'' +
                ", createAt=" + createAt +
                ", formType='" + formType + '\'' +
                ", remark1='" + remark1 + '\'' +
                ", remark2='" + remark2 + '\'' +
                '}';
    }
}
