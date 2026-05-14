package com.shop.util;

import org.springframework.stereotype.Component;

/**
 * 雪花算法 ID 生成器
 *
 * 结构：0 | 41位时间戳 | 10位机器ID | 12位序列号
 * 特点：全局唯一、趋势递增、高性能（每秒可生成几百万个）
 *
 * 生产环境：机器ID 应从配置中心/环境变量获取，确保多实例不重复
 */
@Component
public class SnowflakeIdUtil {

    /** 起始时间戳：2024-01-01 00:00:00，避免 ID 太长 */
    private static final long START_EPOCH = 1704067200000L;

    /** 机器ID所占位数 */
    private static final long WORKER_ID_BITS = 10L;
    /** 序列号所占位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器ID最大值：1023 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    /** 序列号最大值：4095 */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 机器ID左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /** 时间戳左移位数 */
    private static final long TIMESTAMP_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    /** 机器ID（本实例固定为1，多实例需配置） */
    private final long workerId;

    /** 上次生成ID的时间戳 */
    private long lastTimestamp = -1L;
    /** 当前毫秒内的序列号 */
    private long sequence = 0L;

    public SnowflakeIdUtil() {
        // 单机部署固定为1
        this.workerId = 1L;
    }

    /**
     * 生成下一个唯一ID
     * @return 64位 long 型 ID
     */
    public synchronized long nextId() {
        long timestamp = currentMillis();

        // 时钟回拨：正常情况下不会发生，如果发生了说明系统时间被调整
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID: " + (lastTimestamp - timestamp) + "ms");
        }

        if (timestamp == lastTimestamp) {
            // 同一毫秒内，序列号递增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 当前毫秒序列号用完，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        return ((timestamp - START_EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentMillis();
        }
        return timestamp;
    }

    private long currentMillis() {
        return System.currentTimeMillis();
    }
}
