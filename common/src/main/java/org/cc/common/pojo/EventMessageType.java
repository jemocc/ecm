package org.cc.common.pojo;

public enum EventMessageType {
    THREAD_POOL_WATCH("线程池监控"),
    FILE_DOWN_WATCH("文件下载监控")
    ;

    private final String desc
    ;

    EventMessageType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
