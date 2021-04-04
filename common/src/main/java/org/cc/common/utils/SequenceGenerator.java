package org.cc.common.utils;

import java.util.Random;

/**
 * @ClassName: SequenceGenerator
 * @Description: snow-flake-changed
 * timestamp started at 1970-01-01 08:00:00
 * total: 2 + 41 + 12 + 8 + 1
 *  2: fix 01
 * 41: 41bit timestamp, max is 2199023255551(2039-09-07 23:47:35(BeiJin))
 * 12: seq of every milli, max 4095
 *  8: application seq, max 255
 *  1: timestamp rollback
 * @Author: CC
 * @Date 2021/3/30 10:55
 * @ModifyRecords: v1.0 new
 */
public class SequenceGenerator {
    private final int MILLI_SEQ_L = 12;
    private final int APP_SEQ_L = 8;
    private int roll = 0;

    private final Random random;
    private final int applicationSeq;
    private final int MILLI_SEQ_MAX;
    private int end;
    private long lastTimeStamp = System.currentTimeMillis();
    private int milliSeq = 0;

    public SequenceGenerator() {
        int APP_SEQ_MAX = ~(-1 << APP_SEQ_L);
        this.applicationSeq = Integer.parseInt(System.getenv().getOrDefault("APPLICATION_SEQ", "0"));
        if (this.applicationSeq > APP_SEQ_MAX)
            throw new RuntimeException("APP SEQ OVERLOAD");
        this.MILLI_SEQ_MAX = ~(-1 << MILLI_SEQ_L);
        this.random = new Random();
        this.end = applicationSeq << 1 | roll;
    }

    static class Inner {
        public static final SequenceGenerator generator = new SequenceGenerator();
    }

    /**
     * 创建唯一序列值
     * @return  序列值
     */
    public static long newUSeq() {
        return Inner.generator.build_0(true);
    }

    /**
     * 创建随机序列值(同一毫秒不绝对唯一)
     * @return  序列值
     */
    public static long newSeq() {
        return Inner.generator.build_0(false);
    }
    //build sequence
    private long build_0 (boolean unique) {
        if (unique) {
            return seqGen();
        } else {
            int seq = random.nextInt(MILLI_SEQ_MAX);
            return 1L << 62 | timeGen() << (MILLI_SEQ_L + APP_SEQ_L + 1) | seq << (APP_SEQ_L + 1) | end;
        }
    }
    //timestamp generator
    private long timeGen() {
        //start timestamp is the application first run time;
        long start = -1617160444355L;
        return System.currentTimeMillis() + start;
    }
    //unique sequence generator
    private synchronized long seqGen() {
        long now = timeGen();
        if (now < lastTimeStamp) {
            roll ^= 0x01;
            end = applicationSeq << 1 | roll;
            lastTimeStamp = now;
        }
        if (now == lastTimeStamp) {
            milliSeq++;
            if (milliSeq > MILLI_SEQ_MAX) {
                while (now == lastTimeStamp){
                    now = timeGen();
                }
                milliSeq = 0;
            }
        } else {
            milliSeq = 0;
        }
        lastTimeStamp = now;
        return 1L << 62 | now << (MILLI_SEQ_L + APP_SEQ_L + 1) | milliSeq << (APP_SEQ_L + 1) | end;
    }

}
