package org.cc.fileserver.model;

import java.io.Serializable;

/**
 * @ClassName: FileDownProcess
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/8 12:22
 * @ModifyRecords: v1.0 new
 */
public class FileDownProcess implements Serializable {
    private final int id;
    private final String name;
    private int tbc;
    private int dbc = 0;
    private boolean failure = false;

    public FileDownProcess(int id, String name, int tbc) {
        this.id = id;
        this.name = name;
        this.tbc = tbc;
    }

    public boolean isNotFinished() {
        return tbc > dbc && !failure;
    }

    public void setTbc(int tbc) {
        this.tbc = tbc;
    }

    public synchronized void dbcAdd(int dbc) {
        this.dbc += dbc;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void failure() {
        this.failure = true;
    }
}
