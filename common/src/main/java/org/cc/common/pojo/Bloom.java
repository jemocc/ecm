package org.cc.common.pojo;

import java.io.Serializable;

/**
 * @ClassName: Bloom
 * @Description: BloomFilter implements
 * @Author: CC
 * @Date 2021/4/2 9:52
 * @ModifyRecords: v1.0 new
 */
public class Bloom implements Serializable {
    private long val;

    public Bloom(Long val) {
        this.val = val;
    }

    public static Bloom of(Integer val) {
        return val == null ? new Bloom(0L) : new Bloom((long)val);
    }

    public static Bloom of(Long val) {
        return val == null ? new Bloom(0L) : new Bloom(val);
    }

    public Bloom setBit(int bit) {
        this.val |= 0x01 << bit;
        return this;
    }

    public Bloom clearBit(int bit) {
        this.val &= 0x01 << bit;
        return this;
    }

    public boolean checkBit(int bit) {
        return (this.val >> bit & 0x01) > 0;
    }

    public long getVal() {
        return val;
    }
}
