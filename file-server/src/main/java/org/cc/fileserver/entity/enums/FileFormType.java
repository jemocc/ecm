package org.cc.fileserver.entity.enums;

public enum FileFormType {
    LOCAL("本地"),
    REMOTE("远程")
    ;
    private final String cn;

    FileFormType(String cn) {
        this.cn = cn;
    }

    public String getCn() {
        return cn;
    }
}
